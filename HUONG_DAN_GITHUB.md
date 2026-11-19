# Commit và đưa source cập nhật lên GitHub

Gói cập nhật chưa được push hoặc nộp LMS tự động. Hạn theo đề (giờ Việt Nam): tài khoản/OTP trước 16:30 ngày 16/11/2026; video trước 12:45 ngày 19/11/2026. Kiểm tra thời điểm commit thực tế trên GitHub; không sửa thời gian giả.

## Nếu đã có repository bài cũ

1. Clone repo của bạn vào một thư mục mới:

```powershell
git clone URL_REPOSITORY_CUA_BAN
```

2. Copy source bên trong folder `jpa-category` của gói này vào folder vừa clone. Copy cả `.mvn`, `.github`, `.gitignore`. Giữ thư mục `.git` của repo vừa clone. Không copy `target`, `dist`, `data`, file mật khẩu, email demo hoặc video upload.
3. Mở terminal ngay trong folder vừa clone:

```powershell
git status
git add .
git status
git commit -m "Bo sung OTP email, xac thuc tai khoan va CRUD Video"
git push
```

Nếu Git hỏi danh tính, cấu hình `git config user.name "TEN_CUA_BAN"` và `git config user.email "EMAIL_GITHUB_CUA_BAN"` trước commit. Đăng nhập bằng trình duyệt nếu được hỏi.

Kiểm tra staged files không có `config/db.properties`, `config/mail.properties`, `target`, `dist`, `data`. Có thể upload `samples/demo-video.mp4` vì là clip tự tạo nhỏ dùng để test. Không upload video cá nhân dung lượng lớn.

## Nếu tạo repo mới

Tạo repo rỗng, không tạo sẵn README/.gitignore/license vì source đã có. Trong folder source chạy `git init`, `git add .`, `git commit`, `git branch -M main`, `git remote add origin URL_REPO_THAT`, `git push -u origin main`. Nếu repo đã có commit, dùng cách clone phía trên để tránh xung đột lịch sử.

## Upload bằng web nếu chưa dùng Git

Upload: `src`, `scripts`, `database`, `samples`, `config` (chỉ file `.example`), `pom.xml`, `mvnw`, `mvnw.cmd`, README, các file hướng dẫn, `CHAY_DEMO.bat`, `CHAY_MYSQL_SMTP.bat`. `pom.xml` phải ở gốc repository.

Nếu thư mục `.mvn` bị báo hidden, tạo file trực tiếp bằng Add file → Create new file, tên `.mvn/wrapper/maven-wrapper.properties`:

```properties
wrapperVersion=3.3.2
distributionType=only-script
distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip
```

Tạo `.gitignore` trực tiếp nếu không upload được: copy nội dung từ `.gitignore` trong gói. File này cần bỏ qua ít nhất `target/`, `dist/`, `data/`, `config/db.properties`, `config/mail.properties`, `.env`. Upload qua web không tự áp dụng quy tắc .gitignore; phải tự bỏ các mục đó.

`.github` là workflow test, có thể bỏ nếu chỉ upload web, hoặc tạo `.github/workflows/maven.yml` rồi dán nội dung tương ứng từ source. Không nộp chỉ một file ZIP thay cho source.

## Kiểm tra và nộp

Reload repo, mở được source `AuthService.java`, `AppUser.java`, `VideoController.java`, JSP. Nếu dùng Actions, chờ workflow xanh; kết quả test trong gói không phải bằng chứng Actions của tài khoản bạn đã chạy.

Với repo Public, mở link bằng cửa sổ ẩn danh để kiểm tra người chấm xem được. Copy link trang repository, dán vào đúng bài UTExLMS, lưu và bấm Nộp bài nếu có. URL localhost không phải link nộp.
