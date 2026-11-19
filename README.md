# VideoHub – SiteMesh 3, Bootstrap, Validation và Profile

Java 21 · Jakarta Servlet/JSP · Tomcat 11 · JPA/Hibernate · MySQL · SiteMesh 3.3.0 · Bootstrap 5.3.3.

Giữ các chức năng BT03: Category, Users/OTP, Videos CRUD, 10 video mới nhất, 6 video/trang và play video.

Bổ sung: decorator Bootstrap chung, validation client/server, hồ sơ họ tên/số điện thoại/ảnh Cloudinary, đổi email có OTP xác nhận gửi đến email mới.

## Chạy

1. Cài JDK 21; giải nén, mở thư mục có pom.xml.
2. Demo không cần MySQL/SMTP: chạy CHAY_DEMO.bat. Đăng ký/kích hoạt bằng hộp thư demo, không có tài khoản mặc định.
3. Chạy thật: tạo database servletjpa; copy config/db.properties.example và mail.properties.example thành các file bỏ đuôi .example rồi điền cấu hình riêng. Chạy CHAY_MYSQL_SMTP.bat.
4. Mở http://localhost:8080/jpa-category/home ; hồ sơ tại http://localhost:8080/jpa-category/profile .
5. Upload avatar thật cần cấu hình config/cloudinary.properties từ file .example (kể cả khi chạy demo).

Đọc **HUONG_DAN_BT04.md** để cấu hình Cloudinary, test từng yêu cầu và cập nhật từ BT03. Chỉ commit file .example, không commit mật khẩu/API secret, data, target hoặc dist.

## Build/test

Windows: `mvnw.cmd clean verify`. Linux/macOS: `sh mvnw clean verify`.
WAR tạo tại target/jpa-category.war. Bootstrap CSS được đóng gói cục bộ nên không cần CDN khi mở giao diện.
