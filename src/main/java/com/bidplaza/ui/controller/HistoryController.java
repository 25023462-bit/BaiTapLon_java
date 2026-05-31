package com.bidplaza.ui.controller;

import com.bidplaza.network.AuctionSnapshot;
import com.bidplaza.network.Message;
import com.bidplaza.ui.AppStyles;
import com.bidplaza.ui.model.UserSession;
import com.bidplaza.ui.net.ServerClient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class HistoryController implements Initializable {

    @FXML private BorderPane rootPane;
    @FXML private Label totalSessionsLabel;
    @FXML private Label wonCountLabel;
    @FXML private Label totalSpentLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TableView<AuctionSnapshot> historyTable;
    @FXML private TableColumn<AuctionSnapshot, String> colName;
    @FXML private TableColumn<AuctionSnapshot, String> colStarting;
    @FXML private TableColumn<AuctionSnapshot, String> colFinal;
    @FXML private TableColumn<AuctionSnapshot, String> colWinner;
    @FXML private TableColumn<AuctionSnapshot, String> colEndTime;
    @FXML private TableColumn<AuctionSnapshot, String> colStatus;

    private final ObservableList<AuctionSnapshot> allItems = FXCollections.observableArrayList();
    private FilteredList<AuctionSnapshot> filteredItems;

    private static final DateTimeFormatter DTF =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        setupFilters();
        javafx.application.Platform.runLater(() -> {
            if (historyTable.getScene() != null) {
                AppStyles.applyTo(historyTable.getScene());
            }
            loadHistory();
        });
    }

    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList(
            "Tất cả", "WON", "LOST", "FINISHED"));
        statusFilter.setValue("Tất cả");

        filteredItems = new FilteredList<>(allItems, p -> true);
        historyTable.setItems(filteredItems);

        searchField.textProperty().addListener((obs, old, val) -> applyFilter());
        statusFilter.valueProperty().addListener((obs, old, val) -> applyFilter());
    }

    private void setupColumns() {
        colName.setCellValueFactory(c ->
            new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));

        colStarting.setCellValueFactory(c ->
            new javafx.beans.property.SimpleStringProperty(
                String.format("$%.2f", c.getValue().getStartingPrice())));

        colFinal.setCellValueFactory(c ->
            new javafx.beans.property.SimpleStringProperty(
                String.format("$%.2f", c.getValue().getCurrentPrice())));

        colFinal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().remove("history-price-final");
                    return;
                }
                setText(value);
                getStyleClass().add("history-price-final");
            }
        });

        colWinner.setCellValueFactory(c ->
            new javafx.beans.property.SimpleStringProperty(formatWinner(c.getValue())));

        colWinner.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    getStyleClass().remove("history-winner-you");
                    return;
                }
                setText(value);
                if ("Bạn".equals(value)) {
                    getStyleClass().add("history-winner-you");
                } else {
                    getStyleClass().remove("history-winner-you");
                }
            }
        });

        colEndTime.setCellValueFactory(c ->
            new javafx.beans.property.SimpleStringProperty(
                c.getValue().getEndTime() != null
                    ? c.getValue().getEndTime().format(DTF) : "—"));

        colStatus.setCellValueFactory(c ->
            new javafx.beans.property.SimpleStringProperty(displayStatus(c.getValue())));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                String pillClass = switch (value) {
                    case "WON" -> "status-pill-won";
                    case "LOST" -> "status-pill-lost";
                    default -> "status-pill-finished";
                };

                Label label = new Label(value);
                label.getStyleClass().add("status-pill-label");

                HBox pill = new HBox(label);
                pill.getStyleClass().addAll("status-pill", pillClass);

                setText(null);
                setGraphic(pill);
            }
        });
    }

    private void loadHistory() {
        try {
            Message response = ServerClient.request(
                new Message(Message.Type.GET_AUCTION_HISTORY, null));
            if (response.isSuccess() && response.getPayload() instanceof List<?> list) {
                List<AuctionSnapshot> snapshots = list.stream()
                    .filter(AuctionSnapshot.class::isInstance)
                    .map(AuctionSnapshot.class::cast)
                    .toList();
                allItems.setAll(snapshots);
                updateStats();
                applyFilter();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateStats() {
        UserSession session = UserSession.getInstance();
        String username = session.getUsername();
        String userId = session.getUserId();

        int total = allItems.size();
        int won = 0;
        double spent = 0;

        for (AuctionSnapshot snap : allItems) {
            if (isCurrentUserWinner(snap, username, userId)) {
                won++;
                spent += snap.getCurrentPrice();
            }
        }

        totalSessionsLabel.setText(String.valueOf(total));
        wonCountLabel.setText(String.valueOf(won));
        totalSpentLabel.setText(String.format("$%,.2f", spent));
    }

    private void applyFilter() {
        String search = searchField.getText() == null
            ? "" : searchField.getText().toLowerCase().trim();
        String status = statusFilter.getValue();

        filteredItems.setPredicate(snap -> {
            boolean matchSearch = search.isEmpty()
                || snap.getName().toLowerCase().contains(search);
            if ("Tất cả".equals(status) || status == null) {
                return matchSearch;
            }
            String display = displayStatus(snap);
            boolean matchStatus = display.equals(status);
            return matchSearch && matchStatus;
        });
    }

    private String displayStatus(AuctionSnapshot snap) {
        UserSession session = UserSession.getInstance();
        if (isCurrentUserWinner(snap, session.getUsername(), session.getUserId())) {
            return "WON";
        }
        if (snap.getWinnerId() != null || snap.getWinnerUsername() != null) {
            return "LOST";
        }
        return "FINISHED";
    }

    private String formatWinner(AuctionSnapshot snap) {
        UserSession session = UserSession.getInstance();
        if (isCurrentUserWinner(snap, session.getUsername(), session.getUserId())) {
            return "Bạn";
        }
        if (snap.getWinnerUsername() != null) {
            return snap.getWinnerUsername();
        }
        if (snap.getWinnerId() != null) {
            return snap.getWinnerId();
        }
        return "—";
    }

    private boolean isCurrentUserWinner(AuctionSnapshot snap, String username, String userId) {
        if (username != null && username.equals(snap.getWinnerUsername())) {
            return true;
        }
        if (userId != null && userId.equals(snap.getWinnerId())) {
            return true;
        }
        return username != null && username.equals(snap.getWinnerId());
    }

    @FXML
    private void handleRefresh() {
        loadHistory();
    }

    @FXML
    private void handleExportCSV() {
        try {
            String path = com.bidplaza.util.CsvExporter.buildDefaultPath("AuctionHistory");
            String[] headers = {"Tên sản phẩm", "Giá khởi điểm", "Giá cuối", "Người thắng", "Kết thúc lúc", "Trạng thái"};
            java.util.List<String[]> rows = allItems.stream()
                .map(s -> new String[]{
                    s.getName(),
                    String.format("$%.2f", s.getStartingPrice()),
                    String.format("$%.2f", s.getCurrentPrice()),
                    formatWinner(s),
                    s.getEndTime() != null ? s.getEndTime().format(DTF) : "—",
                    displayStatus(s)
                }).collect(java.util.stream.Collectors.toList());
            com.bidplaza.util.CsvExporter.export(headers, rows, path);
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION,
                "Export thành công: " + path);
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR,
                "Lỗi export: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/bidplaza/ui/BidderDashboard.fxml"));
            Scene scene = new Scene(loader.load());
            AppStyles.applyTo(scene);
            Stage stage = (Stage) rootPane.getScene().getWindow();
            if (stage.getScene() != null) {
                javafx.scene.Parent rootNode = scene.getRoot();
                scene.setRoot(new javafx.scene.layout.Pane());
                stage.getScene().setRoot(rootNode);
            } else {
                stage.setScene(scene);
            }
            stage.setMaximized(true);
            
            stage.setTitle("BidPlaza - Bidder Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
