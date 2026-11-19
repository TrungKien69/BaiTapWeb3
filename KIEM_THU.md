# Kết quả kiểm thử BT04

39 kiểm thử tự động đã chạy đạt, 0 lỗi/0 thất bại:

- AccountVideoWebTest: 10 test HTTP/Tomcat – đăng ký OTP, login/logout, reset password, CRUD video, byte-range, latest 10/pagination 6, quyền sở hữu, Multipart, CSRF; thêm profile và email OTP qua HTTP; xác nhận SiteMesh/Bootstrap render một header và thay thẻ body đúng.
- WebIntegrationTest: 5 test HTTP CRUD Category.
- AuthServiceIntegrationTest: 16 test – hash/salt, OTP hết hạn/sai/đã dùng/throttling, khóa login; cập nhật profile, email chưa đổi trước OTP, gửi đúng email mới, ràng buộc target/purpose, rollback khi SMTP lỗi, trùng email và khóa sau 5 lần sai.
- JpaIntegrationTest: 8 test JPA Category, giao dịch và quan hệ.

Build WAR thành công. Bộ test dùng H2 và mail giả/file mailbox. Không gửi SMTP thật, không kết nối MySQL của bạn và không upload lên tài khoản Cloudinary thật trong quá trình test. Cần kiểm tra dịch vụ thật theo HUONG_DAN_BT04.md sau khi điền cấu hình. Chưa kiểm tra bằng trình duyệt đồ họa; giao diện đã được kiểm tra qua response HTTP/JSP thật.

Chạy lại: mvnw.cmd clean verify (Windows), sh mvnw clean verify (Linux/macOS).
