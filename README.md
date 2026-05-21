# BidPlaza - Hệ thống đấu giá trực tuyến

BidPlaza là hệ thống đấu giá trực tuyến mô phỏng quy trình tạo phiên đấu giá, quản lý người dùng, đặt giá, cập nhật giá theo thời gian thực và xử lý nhiều client kết nối đồng thời qua Socket. Project được xây dựng theo hướng hướng đối tượng, có tách lớp model, manager, network, observer và factory để dễ mở rộng.

## Công nghệ sử dụng

| Công nghệ | Mục đích |
| --- | --- |
| Java 17 | Ngôn ngữ lập trình chính |
| JavaFX 21 | Xây dựng giao diện Client |
| Maven | Quản lý build, dependency và test |
| JUnit 5 | Viết và chạy unit test |
| Socket | Giao tiếp realtime giữa Server và Client |

## Cấu trúc project

```text
BidPlaza/
├── pom.xml
├── src/
│   ├── main/java/com/bidplaza/
│   │   ├── Main.java
│   │   ├── exception/
│   │   ├── factory/
│   │   ├── manager/
│   │   ├── model/
│   │   │   ├── item/
│   │   │   └── user/
│   │   ├── network/
│   │   └── observer/
│   ├── java/com/bidplaza/ui/
│   │   ├── controller/
│   │   └── model/
│   ├── resources/com/bidplaza/ui/
│   └── test/java/com/bidplaza/
└── target/
```

### Các package chính

| Package | Chức năng |
| --- | --- |
| `com.bidplaza` | Entry point demo nghiệp vụ đấu giá |
| `com.bidplaza.exception` | Chứa các exception nghiệp vụ như phiên đã đóng hoặc giá đặt không hợp lệ |
| `com.bidplaza.factory` | Tạo các loại sản phẩm đấu giá thông qua Factory Method |
| `com.bidplaza.manager` | Quản lý danh sách phiên đấu giá, sử dụng Singleton cho `AuctionManager` |
| `com.bidplaza.model` | Các entity cốt lõi như `Auction`, `BidTransaction`, `Entity` |
| `com.bidplaza.model.item` | Các loại sản phẩm đấu giá như điện tử, phương tiện, tác phẩm nghệ thuật |
| `com.bidplaza.model.user` | Các loại người dùng như Admin, Seller, Bidder |
| `com.bidplaza.network` | Server, Client, message và handler xử lý kết nối Socket |
| `com.bidplaza.observer` | Cơ chế thông báo khi phiên đấu giá có thay đổi |
| `com.bidplaza.ui` | JavaFX Client, controller, model giao diện và file FXML |
| `com.bidplaza` trong `src/test` | Unit test cho auction, bidder và factory |

## Cài đặt

### Yêu cầu môi trường

- Cài đặt JDK 17.
- Cài đặt Maven.
- Cài đặt JavaFX SDK 21 nếu chạy Client JavaFX.

Kiểm tra phiên bản:

```bash
java -version
mvn -version
```

Build project:

```bash
mvn clean compile
```

Chạy test:

```bash
mvn test
```

## Hướng dẫn chạy Server

Server Socket nằm tại class `com.bidplaza.network.AuctionServer` và lắng nghe ở port `8080`.

```bash
mvn clean compile
java -cp target/classes com.bidplaza.network.AuctionServer
```

Khi chạy thành công, Server sẽ:

- Khởi tạo `AuctionManager`.
- Tạo sẵn một phiên đấu giá mẫu.
- Mở port `8080`.
- Chờ nhiều Client kết nối đồng thời.
- Broadcast thông báo cập nhật giá cho các Client khác.

## Hướng dẫn chạy Client

### Client Socket console

Mở terminal mới sau khi Server đã chạy:

```bash
java -cp target/classes com.bidplaza.network.AuctionClient
```

Nhập lệnh đặt giá theo định dạng:

```text
<auctionId> <bidderId> <amount>
```

Ví dụ:

```text
abc-123 bidder-1 1500
```

Nhập `exit` để thoát Client.

### Client JavaFX

Client JavaFX có entry point:

```text
com.bidplaza.ui.Main
```

Nếu chạy bằng IntelliJ IDEA:

1. Đánh dấu `src/java` là Sources Root.
2. Đánh dấu `src/resources` là Resources Root.
3. Thêm JavaFX SDK 21 vào project libraries.
4. Tạo Run Configuration cho class `com.bidplaza.ui.Main`.
5. Thêm VM options:

```bash
--module-path "PATH_TO_JAVAFX_SDK/lib" --add-modules javafx.controls,javafx.fxml
```

Thay `PATH_TO_JAVAFX_SDK` bằng đường dẫn JavaFX SDK 21 trên máy.

## Design Patterns đã áp dụng

### Singleton

`AuctionManager` được thiết kế theo Singleton để toàn hệ thống dùng chung một instance quản lý các phiên đấu giá.

### Factory Method

`ItemFactory` chịu trách nhiệm tạo các loại item khác nhau như `Electronics`, `Vehicle`, `Art`, giúp tách logic khởi tạo sản phẩm khỏi luồng nghiệp vụ chính.

### Observer

`AuctionObservable`, `BidObserver` và `ConsoleNotifier` triển khai cơ chế Observer để thông báo khi phiên đấu giá có thay đổi, ví dụ khi có lượt đặt giá mới.

## Ghi chú

- Cần chạy Server trước khi chạy Client.
- Có thể mở nhiều Client console cùng lúc để kiểm thử đấu giá đồng thời.
- Port mặc định của Server là `8080`.
