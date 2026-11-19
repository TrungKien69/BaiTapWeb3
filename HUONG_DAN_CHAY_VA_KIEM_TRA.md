# Hướng dẫn chạy và tự kiểm tra trên web

## 1. Mở project bằng VS Code

Giải nén ZIP vào một thư mục mới, ví dụ `D:\BaiTap\jpa-category`. Mở VS Code → File → Open Folder → chọn folder có `pom.xml`. Không chạy trực tiếp trong ZIP.

Cần JDK 21. Kiểm tra trong Terminal:

```powershell
java -version
.\mvnw.cmd -v
```

Cách đơn giản nhất: nhấp đúp `CHAY_DEMO.bat` trong File Explorer. Hoặc terminal tại thư mục project chạy `.\CHAY_DEMO.bat`. Script tự chuyển về đúng folder. Đợi dòng `Mở http://localhost:8080/jpa-category/home` rồi mở URL đó.

Lần đầu cần Internet. Nếu Maven timeout, kiểm tra mạng và chạy lại script (đã có `-U` để thử tải lại). Không xóa database để sửa lỗi mạng. Nếu cổng 8080 bận, dừng Tomcat/project cũ bằng Ctrl+C. Có thể dùng lệnh tương đương dưới với cổng khác:

```powershell
.\mvnw.cmd -U test-compile exec:java "-Dexec.mainClass=vn.iotstar.dev.DevServer" "-Dexec.classpathScope=test" "-Dapp.db=h2" "-Dapp.demo=true" "-Dmail.mode=file" "-Dhttp.port=8081"
```

Khi đổi cổng, thay 8080 bằng 8081 trong mọi URL bên dưới. Mọi URL là **sau context `/jpa-category`**, ví dụ yêu cầu `/video` chạy tại `http://localhost:8080/jpa-category/video`.

## 2. Thử đăng ký và kích hoạt bằng OTP

1. Nhấn **Đăng ký**, hoặc mở `/register`.
2. Điền họ tên, username gồm 3–32 chữ không dấu/số/gạch dưới, email và mật khẩu từ 8 ký tự; nhập lại đúng mật khẩu.
3. Nhấn **Đăng ký và gửi OTP**. Trang chuyển sang `/activate`.
4. Với `CHAY_DEMO.bat`, nhấn **Xem hộp thư demo**. Bạn sẽ thấy thư “Mã kích hoạt tài khoản” và 6 chữ số OTP. Đây là thư cục bộ, không phải Gmail thật.
5. Sao chép mã, nhấn **Quay lại nhập OTP**, nhập đúng email và mã rồi bấm **Kích hoạt tài khoản**.
6. Thành công sẽ chuyển tới đăng nhập. Mã hết hạn sau 5 phút, chỉ dùng một lần.

Tự kiểm tra lỗi: thử đăng nhập trước khi kích hoạt → bị từ chối; thử OTP sai → báo lỗi; dùng lại mã → không cho phép. Muốn gửi lại, dùng form **Gửi lại OTP**, phải đợi ít nhất 60 giây; lấy mã mới nhất trong hộp thư. Sau 5 lần sai phải yêu cầu mã mới. Không dùng hai OTP cho hai mục đích thay thế nhau.

Demo có thể dùng email ví dụ để thử thao tác. Khi thử **gửi mail thật**, phải nhập email nhận mà bạn có quyền kiểm tra.

## 3. Đăng nhập và đăng xuất

1. Mở `/login`, nhập username hoặc email và mật khẩu đã đăng ký.
2. Nhấn **Đăng nhập** → chuyển `/home`, menu xuất hiện **Video của tôi**, **Danh mục**, **Upload Multipart**, **Đăng xuất**.
3. Mở `/video` để kiểm tra truy cập.
4. Nhấn **Đăng xuất** → quay về login. Mở lại `/video` hoặc `/admin/categories` sẽ yêu cầu đăng nhập.

Sai mật khẩu 5 lần sẽ tạm khóa đăng nhập 15 phút. Không có tài khoản demo/mật khẩu mặc định. Khi tài khoản chưa kích hoạt, dùng trang **Chưa kích hoạt?** để gửi lại mã.

## 4. Quên mật khẩu

1. Tại login bấm **Quên mật khẩu?**, nhập email đã đăng ký và kích hoạt.
2. Nhấn **Gửi mã xác nhận** → `/reset-password`.
3. Mở hộp thư demo hoặc email thật tùy chế độ. Lấy mã đặt lại mật khẩu mới nhất.
4. Nhập email, OTP, mật khẩu mới và xác nhận; bấm **Đổi mật khẩu**.
5. Đăng nhập bằng mật khẩu cũ → thất bại; bằng mật khẩu mới → thành công.
6. Nếu tài khoản đang đăng nhập trong một trình duyệt khác, phiên đó bị vô hiệu hóa ở request kế tiếp.

Màn hình gửi mã dùng thông báo chung để không xác nhận công khai email có tồn tại. Nếu email chưa đăng ký/chưa kích hoạt thì không có thư reset. Không reset được bằng OTP kích hoạt cũ.

## 5. Thêm video bằng Multipart

1. Đăng nhập, nhấn **Video của tôi → Thêm video**.
2. Nhập tiêu đề, mô tả. Danh mục có thể để **Không chọn** hoặc tạo trước qua menu Danh mục.
3. Trong **Upload video**, chọn `samples/demo-video.mp4` kèm trong gói (3 giây, có hình và tiếng).
4. Có thể chọn ảnh bìa PNG/JPG/GIF/WebP không quá 2 MB, hoặc nhập link ảnh.
5. Chọn **Công khai**, nhấn **Đăng video**.
6. Video xuất hiện ở `/video` và `/home`. Tệp được lưu ngoài thư mục deploy, tên file UUID do server tạo.

Video tối đa **100 MB**, tổng request tối đa khoảng 102 MB. Server nhận MP4/WebM/OGG, kiểm tra dấu hiệu định dạng thay vì tin tên file. Đây không phải bộ chuyển đổi codec hay bộ quét nội dung. Với file tự quay không phát được, thử xuất MP4 H.264/AAC hoặc dùng clip mẫu. Có thể thay upload bằng **link video trực tiếp**; không nhập URL trang YouTube. Link từ website khác có thể bị chặn nhúng, khi đó dùng upload từ máy.

Menu **Upload Multipart** mở `/upload-demo`: cùng nghiệp vụ lưu Video nhưng minh họa đúng tên trường `multiPartServlet` và endpoint `/multiPartServlet` trong Form3. Form dùng `enctype="multipart/form-data"`, controller có `@MultipartConfig`, `request.getPart`, lưu `Part.write`.

## 6. Sửa và xóa video

- `/video` → bấm **Sửa** trên thẻ của mình. Đổi tiêu đề/mô tả/trạng thái, thay ảnh hoặc video nếu cần. Không chọn file mới và để trống link sẽ giữ file cũ.
- Chọn **Ẩn** → video vẫn nằm trong thư viện cá nhân, không hiện ở home và người khác không được xem.
- Bấm **Xóa**, xác nhận → xóa bản ghi và file video/ảnh upload tương ứng. Link ngoài không bị server xóa.
- Tạo tài khoản thứ hai rồi thử URL sửa/xóa video tài khoản thứ nhất: bị từ chối (404). Server lấy owner từ phiên đăng nhập, không tin user ID do form gửi.

## 7. Trang chủ, phân trang và play

- Tạo ít nhất **11 video công khai** để kiểm tra `/home` chỉ hiện **10 video mới nhất**. Tiêu đề video mới nhất nằm trước.
- Tạo ít nhất **7 video** trong cùng tài khoản để kiểm tra `/video`: trang 1 có 6 thẻ, bấm **Sau** sẽ thấy phần còn lại ở trang 2.
- Mỗi tài khoản chỉ thấy video của mình trên `/video`; home là các video công khai của cộng đồng.
- Bấm **tiêu đề video** trên home hoặc thư viện → `/video/detail?id=...`.
- Nhấn Play, kiểm tra hình và tiếng; thử tua. Video upload được phục vụ qua `/media/video?id=...`, hỗ trợ HTTP byte ranges. Video link ngoài được trình duyệt tải trực tiếp.
- Trang chi tiết tăng lượt xem. Video ẩn chỉ chủ sở hữu xem được.

## 8. Gửi OTP email thật với MySQL

1. Trong MySQL Workbench chạy `database/01_create_database.sql` để tạo **servletjpa** nếu chưa có.
2. Copy `config/db.properties.example` thành `config/db.properties`; sửa user/password MySQL.
3. Copy `config/mail.properties.example` thành `config/mail.properties`.
4. Ví dụ Gmail, sửa thành thông tin của bạn:

```properties
mail.mode=smtp
mail.host=smtp.gmail.com
mail.port=587
mail.ssl=false
mail.username=EMAIL_GUI_THAT@gmail.com
mail.password=MAT_KHAU_UNG_DUNG_SMTP
mail.from=EMAIL_GUI_THAT@gmail.com
```

Google yêu cầu tài khoản bật xác minh 2 bước và được phép tạo App Password. Không điền mật khẩu Gmail thông thường. Nếu tài khoản trường/tổ chức không cho tạo App Password, dùng tài khoản hoặc nhà cung cấp SMTP khác phù hợp. Xem [hướng dẫn Google](https://support.google.com/accounts/answer/185833).

5. Dừng demo bằng Ctrl+C, chạy **CHAY_MYSQL_SMTP.bat**.
6. Đăng ký với email nhận thật mà bạn kiểm tra được. OTP phải đến hộp thư email (kiểm tra Spam). Ở chế độ này `/demo/mail` trả 404, không có link lấy OTP demo thay thế.
7. Kích hoạt và thử quên mật khẩu qua email thật.

Lưu ý: H2 và MySQL là hai database khác nhau; tài khoản demo H2 không tự có trong MySQL. Cần đăng ký tài khoản khi chuyển DB. Thông báo gửi thành công không đảm bảo mail không bị nhà cung cấp chuyển Spam.

Cấu hình SMTP dùng STARTTLS bắt buộc với cổng 587; muốn SSL 465 đặt `mail.port=465`, `mail.ssl=true`. Không tắt kiểm tra chứng chỉ để xử lý lỗi TLS. Kiểm tra tài khoản, App Password và kết nối mạng.

## 9. Kiểm tra dữ liệu trong Workbench

Sau khi chạy MySQL lần đầu:

```sql
USE servletjpa;
SHOW TABLES;
SELECT id, username, email, active, session_version, password_hash FROM users;
SELECT purpose, expiresAt, sentAt, attempts, used FROM otp_tokens;
SELECT VideoId, Title, Active, user_id, source, created_at FROM videos;
```

`password_hash` bắt đầu bằng `pbkdf2_sha256$`, không phải mật khẩu gốc. Trong `otp_tokens`, `code_hash` là hash, không lưu 6 số OTP thô. Cột `user_id` của video mới khớp `users.id` người đăng nhập. Có thể xem khóa ngoại trong sơ đồ Workbench hoặc `SHOW CREATE TABLE videos;`.

Nếu dùng database cũ, sao lưu trước khi nâng cấp và xem `database/03_kiem_tra_nang_cap.sql`. Không chạy DROP DATABASE để cập nhật bài.

## 10. Chạy WAR trên Tomcat 11

- Cần JDK 21 và Tomcat 11. Bản đóng gói có sẵn `dist/jpa-category.war`; hoặc build `.\mvnw.cmd clean package`.
- Dừng Tomcat trước khi cập nhật WAR. Copy WAR vào `webapps`; xóa thư mục deploy `webapps/jpa-category` cũ nếu cần triển khai lại. Giữ nguyên DB và thư mục upload.
- Trong `Tomcat/bin/setenv.bat`, đặt `JAVA_HOME` đúng thư mục JDK, `DB_USER`, `DB_PASSWORD` (và `DB_URL` nếu khác mặc định), `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`. Không commit file này.
- Hoặc đặt Java properties `-Ddb.config=D:/duong-dan/config/db.properties -Dmail.config=D:/duong-dan/config/mail.properties` trong `CATALINA_OPTS`. Tomcat ngoài không tự tìm cấu hình nằm trong folder source.
- Chạy `Tomcat/bin/catalina.bat run`, mở `http://localhost:8080/jpa-category/home`.
- Mặc định WAR dùng MySQL + SMTP, không bật hộp thư demo. `UPLOAD_DIR` đổi thư mục ảnh; `VIDEO_DIR` đổi thư mục video. Bảo đảm tiến trình Tomcat có quyền ghi vào các thư mục này.

## 11. Thử tự động

Chạy `.\mvnw.cmd clean verify`. Bộ test sử dụng H2 in-memory và hộp thư file tạm, không gửi email ra ngoài, không dùng dữ liệu demo. Báo cáo nằm trong `target/surefire-reports`.

Các kiểm tra bao gồm: hash có salt; tài khoản chưa kích hoạt không đăng nhập được; OTP sai/hết hạn/dùng lại; giới hạn gửi/thử; reset vô hiệu phiên; đăng nhập/đăng xuất; CRUD Category; CRUD Video theo chủ sở hữu; phân trang 6; top 10; Multipart; HTTP Range và tệp video mẫu thật.

Kiểm thử tự động không thay thế xác nhận email thật đến hộp thư hoặc xem/play trên trình duyệt/máy của bạn.
