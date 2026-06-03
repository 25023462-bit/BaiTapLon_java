
# BidPlaza - Hệ thống đấu giá trực tuyến

BidPlaza là hệ thống đấu giá trực tuyến mô phỏng quy trình tạo phiên đấu giá, quản lý người dùng, đặt giá, cập nhật giá theo thời gian thực và xử lý nhiều client kết nối đồng thời qua Socket. Project được xây dựng theo hướng hướng đối tượng, có tách lớp model, manager, network, observer và factory để dễ mở rộng.

## 📄 Báo cáo & Video Demo

[![PDF](https://img.shields.io/badge/📄%20Báo%20cáo-PDF-red?style=for-the-badge)](./Báo-cáo-và-VIDEO.pdf)

> 📥 [**Tải báo cáo và video demo tại đây**](./Báo-cáo-và-VIDEO.pdf)

---

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
├── .github/workflows/ci.yml
├── src/
│   ├── main/java/com/bidplaza/
│   │   ├── exception/         (InvalidBidException, AuctionClosedException, AuthenticationException)
│   │   ├── factory/           (ItemFactory – Factory Method)
│   │   ├── manager/           (AuctionManager – Singleton, UserManager)
│   │   ├── model/             (Auction, BidTransaction, Entity, Review)
│   │   │   ├── item/          (Item, Electronics, Vehicle, Art)
│   │   │   └── user/          (User, Bidder, Seller, Admin)
│   │   ├── network/           (AuctionServer, ClientHandler, AuctionClient,
│   │   │                       Message, AuctionSnapshot, AuctionTimer,
│   │   │                       ChatMessage, DepositRequest, ReviewRequest, ...)
│   │   ├── observer/          (BidObserver, AuctionObservable, ConsoleNotifier)
│   │   ├── storage/           (DataStorage – Serialization)
│   │   └── util/              (CsvExporter)
│   ├── main/java/com/bidplaza/ui/
│   │   ├── controller/        (11 controllers: Login, Welcome, AuctionList,
│   │   │                       AuctionDetail, BidderDashboard, SellerDashboard,
│   │   │                       AdminDashboard, Profile, History,
│   │   │                       Watchlist, Deposit)
│   │   ├── model/             (UserSession, AuctionItem)
│   │   └── net/               (ServerClient)
│   ├── main/resources/com/bidplaza/ui/
│   │   ├── *.fxml             (12 FXML screens)
│   │   └── style.css
│   └── test/java/com/bidplaza/
│       ├── AuctionTest.java            (12 tests)
│       ├── AuctionConcurrencyTest.java  (2 tests)
│       ├── AuctionExceptionTest.java    (5 tests)
│       ├── BidderTest.java              (6 tests)
│       ├── BidderExtendedTest.java      (7 tests)
│       ├── ItemFactoryTest.java         (5 tests)
│       ├── ServerIntegrationTest.java   (3 tests)
│       ├── UserManagerTest.java         (7 tests)
│       ├── CsvExporterTest.java         (5 tests)
│       ├── ObserverTest.java            (3 tests)
│       ├── DataStorageTest.java         (3 tests)
│       ├── NetworkDtoTest.java
│       ├── ManagerExtendedTest.java
│       ├── SellerReviewTest.java
│       └── ui/FxmlLoadTest.java         (@Disabled)
```

## Tính năng

### Tính năng bắt buộc
| Tính năng | Mô tả |
|---|---|
| Quản lý người dùng | Đăng ký / đăng nhập với 3 vai trò: Bidder, Seller, Admin |
| Quản lý sản phẩm | Seller tạo, sửa, xóa sản phẩm đấu giá |
| Đấu giá realtime | Đặt giá, kiểm tra hợp lệ, cập nhật ngay cho tất cả client |
| State machine | Phiên đấu giá chuyển trạng thái: OPEN → RUNNING → FINISHED → PAID/CANCELED |
| Tự động kết thúc | AuctionTimer kiểm tra mỗi 10 giây, tự đóng phiên hết hạn |
| Xử lý lỗi | Custom exceptions: InvalidBidException, AuctionClosedException, AuthenticationException |
| Concurrency | ReentrantLock + synchronized tránh race condition khi nhiều client bid cùng lúc |
| Giao diện JavaFX | 13 màn hình FXML với CSS styling |

### Tính năng nâng cao
| Tính năng | Mô tả |
|---|---|
| Anti-sniping | Tự động gia hạn phiên 60 giây nếu có bid trong 30 giây cuối |
| Auto-bidding | Đặt maxBid + increment, hệ thống tự đặt giá thay người dùng |
| LineChart | Biểu đồ đường giá realtime cập nhật sau mỗi bid |
| Countdown timer | Đếm ngược thời gian còn lại, đổi màu khi gần kết thúc |
| Chat trong phòng | Bidder nhắn tin realtime trong cùng phiên đấu giá |
| Outbid notification | Popup cảnh báo khi bị người khác vượt giá |
| Tìm kiếm & lọc | Tìm theo tên sản phẩm, lọc theo danh mục và trạng thái |
| Watchlist | Bookmark phiên đấu giá yêu thích |
| Export CSV | Xuất lịch sử đấu giá/giao dịch ra file CSV |
| Rating seller | Người thắng đánh giá 1–5 sao sau phiên kết thúc |
| Nạp tiền | Bidder nạp tiền vào tài khoản qua giao diện |
| Lịch sử giao dịch | Xem tất cả lịch sử bid cá nhân |

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

## Hướng dẫn chạy Client JavaFX

- Chạy server: chạy class `AuctionServer` trong `com.bidplaza.network`.
- Chạy client: chạy class `Main` trong `com.bidplaza` hoặc chạy file `run-client.bat`.
- Nếu đã cấu hình đúng pom.xml, có thể chạy bằng Maven:
```bash
mvn javafx:run
```

## Design Patterns

- Singleton: `AuctionManager` quản lý duy nhất một danh sách phiên đấu giá.
- Factory: `ItemFactory` tạo các loại sản phẩm đấu giá.
- Observer: `BidObserver`, `AuctionObservable` cập nhật realtime cho client.
- MVC: Tách Controller / Model / View trong JavaFX client.
- Strategy: Xử lý auto-bidding với maxBid và increment.

## Unit Tests

Dự án có **74 unit tests** chia thành nhiều test class:

| Test Class | Tests | Nội dung |
|---|---|---|
| AuctionTest | 12 | Logic đấu giá, state machine, anti-sniping |
| AuctionConcurrencyTest | 2 | 5–10 threads bid đồng thời |
| AuctionExceptionTest | 5 | InvalidBidException, AuctionClosedException |
| BidderTest | 6 | Thông tin bidder, balance |
| BidderExtendedTest | 7 | Watchlist, deposit, canBid |
| ItemFactoryTest | 5 | Tạo Electronics, Vehicle, Art |
| ServerIntegrationTest | 3 | Client–Server communication |
| UserManagerTest | 7 | Đăng ký, đăng nhập, duplicate, role |
| CsvExporterTest | 5 | Xuất file CSV, special chars |
| ObserverTest | 3 | Add/remove observer, notify |
| DataStorageTest | 3 | Save/load serialization |
| NetworkDtoTest | 9 | Network data objects |
| ManagerExtendedTest | 3 | AuctionManager, UserManager |
| SellerReviewTest | 3 | Rating, review validation |

Tổng: **74 tests, 0 failures**
JaCoCo instruction coverage: *61.84%* (UI packages excluded)

Chạy toàn bộ test:
```bash
mvn test
```

## CI/CD

Project sử dụng GitHub Actions để tự động build và test mỗi khi push code.

File cấu hình: `.github/workflows/ci.yml`

Các bước tự động:
1. Checkout code
2. Setup JDK 17
3. mvn clean compile
4. mvn test

Badge CI/CD sẽ hiển thị trạng thái build trên GitHub.

## Thành viên & Đóng góp

| Thành viên | MSSV | Phần phụ trách |
|---|---|---|
| [Nguyễn Huy Đức] | [25023466] | Backend: Server, concurrency, business logic |
| [Trần Đức Đạt] | [25023462] | Frontend: JavaFX UI, controllers, FXML |
| [Lê Văn Đạt] | [25023460] | Testing, integration, CI/CD |
| [Đỗ Thành Đạt] | [25023458] | Database, Advanced features, documentation |


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
