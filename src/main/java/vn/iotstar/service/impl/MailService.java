package vn.iotstar.service.impl;
import vn.iotstar.service.MailSender;
import vn.iotstar.config.Settings;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
public class MailService implements MailSender {
    public static Path outbox(){return Path.of(Settings.get("mail.outbox","data/mail-outbox"));}
    public void send(String to,String purpose,String otp) {
        String subject=purpose.equals("EMAIL_CHANGE")?"Xác nhận đổi email hồ sơ":purpose.equals("ACTIVATE")?"Mã kích hoạt tài khoản":"Mã đặt lại mật khẩu";
        String body="Mã OTP của bạn: "+otp+"\nMã có hiệu lực 5 phút, chỉ dùng một lần. Không chia sẻ mã này.\nNếu bạn không yêu cầu, hãy bỏ qua thư này.";
        if(Settings.demoMail()){
            try{Files.createDirectories(outbox());Files.writeString(outbox().resolve(System.currentTimeMillis()+"-"+UUID.randomUUID()+".eml"),"To: "+to+"\nSubject: "+subject+"\n\n"+body,StandardCharsets.UTF_8);return;}
            catch(java.io.IOException e){throw new IllegalStateException("Không ghi được hộp thư demo",e);}
        }
        if(!"smtp".equals(Settings.get("mail.mode","smtp")))throw new IllegalStateException("Hộp thư file chỉ được dùng trong chế độ demo cục bộ");
        String host=Settings.get("mail.host",""),username=Settings.get("mail.username",""),password=Settings.get("mail.password","");
        if(host.isBlank()||username.isBlank()||password.isBlank())throw new IllegalStateException("Chưa cấu hình SMTP. Hãy sửa config/mail.properties hoặc dùng CHAY_DEMO.bat để thử cục bộ.");
        boolean ssl=Boolean.parseBoolean(Settings.get("mail.ssl","false"));
        Properties p=new Properties();p.setProperty("mail.smtp.host",host);p.setProperty("mail.smtp.port",Settings.get("mail.port",ssl?"465":"587"));
        p.setProperty("mail.smtp.auth","true");p.setProperty("mail.smtp.ssl.enable",Boolean.toString(ssl));
        p.setProperty("mail.smtp.ssl.checkserveridentity","true");p.setProperty("mail.smtp.starttls.enable",Boolean.toString(!ssl));p.setProperty("mail.smtp.starttls.required",Boolean.toString(!ssl));
        p.setProperty("mail.smtp.connectiontimeout","10000");p.setProperty("mail.smtp.timeout","10000");p.setProperty("mail.smtp.writetimeout","10000");
        Session session=Session.getInstance(p,new Authenticator(){protected PasswordAuthentication getPasswordAuthentication(){return new PasswordAuthentication(username,password);}});
        try{MimeMessage msg=new MimeMessage(session);msg.setFrom(new InternetAddress(Settings.get("mail.from",username)));msg.setRecipient(Message.RecipientType.TO,new InternetAddress(to));msg.setSubject(subject,"UTF-8");msg.setText(body,"UTF-8");Transport.send(msg);}
        catch(MessagingException e){throw new IllegalStateException("Gửi email thất bại. Kiểm tra host, cổng, tài khoản và mật khẩu ứng dụng SMTP.",e);}
    }
}
