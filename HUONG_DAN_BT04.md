# Hướng dẫn phần bổ sung BT04

## 1. Cập nhật từ BT03

- Dừng web cũ bằng Ctrl+C. Giải nén bản mới vào một thư mục riêng.
- Copy config/db.properties và config/mail.properties từ máy bạn sang config của bản mới. Không đưa các file thật lên GitHub.
- Tiếp tục sử dụng database servletjpa. Với db.schema=update, Hibernate tự thêm các cột phone, images, pendingEmail vào users; không cần xóa database.
- Để giữ file video/ảnh đã upload, copy thư mục data từ bản cũ sang thư mục gốc project mới (hoặc giữ cấu hình video.dir/upload.dir nếu trước đó bạn dùng đường dẫn riêng). Source GitHub không chứa dữ liệu trên máy bạn.
- Chạy CHAY_MYSQL_SMTP.bat. Mở http://localhost:8080/jpa-category/home .

## 2. Cloudinary – upload ảnh đại diện thật

1. Tạo/đăng nhập tài khoản Cloudinary, mở Console/Settings/API Keys.
2. Lấy Cloud name, API Key và API Secret của môi trường của bạn.
3. Copy config/cloudinary.properties.example thành config/cloudinary.properties.
4. Điền ba khóa cloudinary.cloud_name, cloudinary.api_key, cloudinary.api_secret, không thêm dấu nháy.
5. Đăng nhập web → Hồ sơ → nhập họ tên và phone hợp lệ → chọn PNG/JPEG <=2 MB, <=16 triệu pixel → Lưu hồ sơ.
6. Ảnh được gửi multipart/form-data từ browser tới Servlet bằng Part, rồi backend dùng Cloudinary Java SDK upload bằng API có ký xác thực. DB chỉ lưu secure_url ở users.images.
7. Kiểm tra ảnh hiện trên /profile và trong Media Library của Cloudinary, thư mục videohub/avatars. Tải lại trang/đăng nhập lại để xác nhận URL đã lưu.

Không cần unsigned upload preset. Secret chỉ nằm phía server, không ghi vào JSP/JavaScript/GitHub. Nếu chưa cấu hình Cloudinary, vẫn cập nhật họ tên/phone được khi không chọn ảnh; chọn ảnh sẽ có thông báo cần cấu hình. Demo cũng không giả lập upload Cloudinary.

## 3. Test SiteMesh và Bootstrap

- Đăng xuất, mở login/register/forgot-password: đều có cùng header/footer.
- Đăng nhập, mở /home, /video, thêm/sửa video, danh mục, /profile: đều dùng cùng decorator.
- Xem mã nguồn trang: có data-layout="sitemesh-bootstrap", liên kết assets/bootstrap.min.css, không xuất hiện thẻ sitemesh:write chưa xử lý, header chỉ xuất hiện một lần.
- Thu nhỏ trình duyệt: bố cục profile Bootstrap row/col chuyển từ hai cột xuống một cột.
- Cấu hình: WEB-INF/sitemesh3.xml; template duy nhất: WEB-INF/decorators/bootstrap.jsp; filter trong WEB-INF/web.xml. Tệp video/ảnh/CSS không bị decorator bọc HTML.
- Tomcat 11: bản này có META-INF/context.xml với suspendWrappedResponseAfterForward=false, DevServer cũng đặt tương tự để tương thích SiteMesh. Nếu dùng Tomcat ngoài có chính sách không đọc context.xml trong WAR, thêm thuộc tính này vào Context của ứng dụng trên Tomcat.

## 4. Test validation

| Form | Thao tác sai | Mong đợi |
|---|---|---|
| Login | Trống identity/password, sai password | Chặn trống; thông báo sai đăng nhập |
| Register | Email sai; username <3 ký tự; tên toàn dấu cách; password <8; confirm khác | Chặn/thông báo, không tạo tài khoản |
| Forgot password | Email trống hoặc không đúng cấu trúc | Báo lỗi; không gửi OTP |
| Reset/Activate | OTP không đủ 6 số, OTP sai/hết hạn/dùng lại | Không chấp nhận |
| Insert/Edit Category | Tên trống/toàn dấu cách, trùng tên, >50 ký tự, ảnh sai | Không lưu dữ liệu không hợp lệ |
| Insert/Edit Video | Thiếu title hoặc nguồn video; file sai/ quá 100 MB | Không lưu; có thông báo |
| Profile | Tên trống/>100, phone chứa chữ/<9/>15 số | Không cập nhật |
| Avatar | TXT đổi tên JPG, ảnh >2 MB | Từ chối, giữ ảnh cũ |

Client: HTML5 required/type/pattern/maxlength + assets/validation.js (Bootstrap form-control/was-validated, xác nhận password, dung lượng file). Server: AuthService, VideoService, CategoryServiceImpl và kiểm tra nội dung file thật. Tắt JS rồi gửi dữ liệu sai vẫn phải bị server từ chối. Các form POST có CSRF.

## 5. Test hồ sơ

1. Đăng nhập A → Hồ sơ (/profile).
2. Sửa fullname/phone (ví dụ 0901234567), không chọn ảnh → lưu → tải lại: thông tin mới giữ nguyên.
3. Chọn PNG/JPEG → lưu → ảnh hiển thị; kiểm tra Cloudinary như phần 2.
4. Lần sau sửa tên không chọn ảnh: ảnh trước vẫn giữ nguyên.
5. Đăng xuất và nhập /profile: chuyển đến /login.
6. Đăng nhập B: chỉ cập nhật hồ sơ B; server lấy userId từ session, không nhận ID người khác từ form.

## 6. Test đổi email bằng OTP

Đổi email là form riêng, để bạn có thể cập nhật họ tên/phone độc lập mà không chờ OTP.

1. Trong /profile, nhập email mới mà bạn nhận được thư → Gửi OTP.
2. Hệ thống hiển thị email đang chờ xác nhận, nhưng email hiện tại giữ nguyên trong DB.
3. Mở hộp thư email mới (kiểm tra Spam), lấy OTP 6 số. Demo có link hộp thư demo trong trang hồ sơ.
4. Nhập sai OTP: hiện lỗi, email cũ vẫn giữ.
5. Nhập đúng OTP: thông báo thành công, users.email mới được cập nhật, pendingEmail xóa.
6. Đăng xuất → đăng nhập bằng email mới và password cũ: thành công; email cũ không đăng nhập được (username vẫn đăng nhập được).
7. Thử OTP đã dùng lại: từ chối. Thử email đã có tài khoản khác: từ chối.
8. Gửi mã mới: đợi >=60 giây, gửi lại cùng email; mã trước mất hiệu lực. Mã hết hạn sau 5 phút; sai 5 lần bị khóa mã, cần yêu cầu mã mới sau thời gian chờ.
9. Hủy yêu cầu: OTP đó không còn dùng được. Mã ACTIVATE/RESET không thể dùng để đổi email.
10. SMTP gửi lỗi: không đổi email và không lưu yêu cầu mới dang dở.

## 7. Kiểm tra MySQL

```sql
USE servletjpa;
SELECT id, username, full_name, phone, email, pendingEmail, images FROM users;
```

Trước xác nhận OTP: email cũ + pendingEmail mới. Sau xác nhận: email mới + pendingEmail NULL. images là URL https Cloudinary.

## 8. Build, nộp GitHub và giới hạn kiểm tra

Chạy mvnw.cmd clean verify; WAR tạo tại target/jpa-category.war. Nếu deploy WAR mới, dừng Tomcat và thay cả WAR/thư mục ứng dụng cũ để tránh cache JSP.

Upload source, config/*.example, .mvn/wrapper/maven-wrapper.properties, .github/workflows/maven.yml đúng cấu trúc. Không upload file mật khẩu thật, data, target, dist.

Các kiểm thử tự động dùng H2, Tomcat HTTP thật, SMTP giả/file mailbox; không dùng MySQL hoặc Cloudinary của người dùng. Cần tự thực hiện test Cloudinary/SMTP thật sau khi điền cấu hình. Không có thông tin đăng nhập mặc định.

## Tài liệu tham khảo

- https://github.com/sitemesh/sitemesh3
- https://cloudinary.com/documentation/java_image_and_video_upload
- https://getbootstrap.com/docs/5.3/forms/validation/
