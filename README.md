# Fashion Shop Backend (`fashion-be`) — Đồ án Phát triển Phần mềm Hướng Đối Tượng

Backend RESTful API xây dựng bằng **Java 17**, **Spring Boot 4**, **Gradle**, **Spring Data JPA**, **Spring Security (JWT)** cho đề tài **Website Quản lý Bán Quần Áo**.

## 1. Điểm nhấn Thiết kế Hướng Đối Tượng (OOP & Design Patterns)

1. **Tính Trừu tượng (`Abstraction`) & Kế thừa (`Inheritance`):**
   - `BaseEntity` (`@MappedSuperclass`): Lớp cơ sở trừu tượng dùng chung `id`, `createdAt`, `updatedAt` cho toàn bộ 16 thực thể nghiệp vụ.
   - `DiscountStrategy`: Interface trừu tượng hóa thuật toán tính khuyến mãi.
2. **Tính Đóng gói (`Encapsulation` - Rich Domain Model):**
   - `ProductVariant`: Đóng gói trực tiếp nghiệp vụ kho `importStock(qty, unitCost)` (cộng tồn kho & tính giá vốn bình quân gia quyền), `deductStock(qty)` (kiểm tra & trừ tồn kho khi đặt hàng), `restoreStock(qty)` (hoàn kho khi hủy đơn).
   - `Voucher`: Đóng gói kiểm tra điều kiện áp dụng (`validateApplicable`) và vòng đời mã giảm giá (`consumeUsage`, `startNow`, `endEarly`, `cancel`).
   - `Order`: Đóng gói tính toán tổng tiền (`recalculateTotals`) và chuyển trạng thái hợp lệ (`transitionTo`).
3. **Tính Đa hình (`Polymorphism`) & 3 Design Patterns:**
   - **Strategy Pattern:** `PercentageDiscountStrategy` & `FixedAmountDiscountStrategy` cùng thực thi `DiscountStrategy`.
   - **Factory Pattern:** `DiscountStrategyFactory` cấp phát đúng chiến lược giảm giá theo `DiscountType`.
   - **State Pattern (State Machine):** `OrderStatus` kiểm soát chặt chẽ chuyển đổi trạng thái đơn hàng (`PENDING` → `PROCESSING` → `SHIPPING` → `COMPLETED` / `CANCELLED`).

## 2. Tài khoản Mẫu (Tự động khởi tạo bởi `DataInitializer`)

- **Quản trị viên (`ADMIN`):** `admin@luxe.vn` / `admin123`
- **Khách hàng (`CUSTOMER`):** `khachhang@gmail.com` / `123456`
- **Mã giảm giá đang hoạt động:** `WELCOME50`, `LUXE10`, `VIP200`

## 3. Cách chạy Dự án

### Chạy ngay với H2 File Database (Mặc định - không cần cài đặt thêm):
```powershell
.\gradlew.bat bootRun
```
Server khởi động tại `http://localhost:8080`. Dữ liệu lưu bền vững tại `./data/fashiondb`.

### Chạy với PostgreSQL qua Docker Compose (Tùy chọn):
```powershell
docker compose up -d
.\gradlew.bat bootRun --args="--spring.profiles.active=postgres"
```

### Chạy Bộ Kiểm thử OOP & Tích hợp (JUnit):
```powershell
.\gradlew.bat test
```

