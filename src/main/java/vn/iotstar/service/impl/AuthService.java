package vn.iotstar.service.impl;
import vn.iotstar.entity.*;
import vn.iotstar.config.Db;
import vn.iotstar.security.PasswordHasher;
import vn.iotstar.service.MailSender;
import jakarta.persistence.*;
import java.time.*;
import java.util.*;

public class AuthService {
    private final MailSender mail; private final Clock clock;
    public AuthService(){this(new MailService(),Clock.systemUTC());}
    public AuthService(MailSender mail,Clock clock){this.mail=mail;this.clock=clock;}
    public static String email(String value){String e=value==null?"":value.trim().toLowerCase(Locale.ROOT);if(e.length()>254||!e.matches("[^\\s@]+@[^\\s@]+[.][^\\s@]+"))throw new IllegalArgumentException("Email không hợp lệ");return e;}
    public static void password(String p){if(p==null||p.length()<8||p.length()>128)throw new IllegalArgumentException("Mật khẩu cần từ 8 đến 128 ký tự");}
    private AppUser byEmail(EntityManager em,String email){return em.createQuery("SELECT u FROM AppUser u WHERE u.email=:email",AppUser.class).setParameter("email",email).getResultStream().findFirst().orElse(null);}
    private OtpToken token(EntityManager em,AppUser u,String purpose){return em.createQuery("SELECT t FROM OtpToken t WHERE t.user.id=:id AND t.purpose=:p",OtpToken.class).setParameter("id",u.getId()).setParameter("p",purpose).getResultStream().findFirst().orElse(null);}
    private void issue(EntityManager em,AppUser u,String purpose){
        OtpToken t=token(em,u,purpose);Instant now=clock.instant();
        if(t!=null&&t.getSentAt().plusSeconds(60).isAfter(now))throw new IllegalArgumentException("Vui lòng đợi 60 giây trước khi yêu cầu mã mới");
        boolean fresh=t==null;if(fresh){t=new OtpToken();t.setUser(u);t.setPurpose(purpose);}
        String previous=t.getCodeHash(),code=PasswordHasher.otp();while(previous!=null&&PasswordHasher.verify(code,previous))code=PasswordHasher.otp();t.setCodeHash(PasswordHasher.hash(code));t.setAttempts(0);t.setUsed(false);t.setSentAt(now);t.setExpiresAt(now.plusSeconds(300));
        if(fresh)em.persist(t);
        mail.send("EMAIL_CHANGE".equals(purpose)?u.getPendingEmail():u.getEmail(),purpose,code);
    }
    public AppUser register(String username,String email,String fullName,String password){
        String e=email(email),name=username==null?"":username.trim().toLowerCase(Locale.ROOT),full=fullName==null?"":fullName.trim();password(password);
        if(!name.matches("[a-z0-9_]{3,32}"))throw new IllegalArgumentException("Tên đăng nhập gồm 3–32 chữ không dấu, số hoặc dấu gạch dưới");
        if(full.isEmpty()||full.length()>100)throw new IllegalArgumentException("Họ tên cần từ 1 đến 100 ký tự");
        String hash=PasswordHasher.hash(password);
        return Db.write(em->{
            if(byEmail(em,e)!=null||em.createQuery("SELECT COUNT(u) FROM AppUser u WHERE u.username=:n",Long.class).setParameter("n",name).getSingleResult()>0)throw new IllegalArgumentException("Tên đăng nhập hoặc email đã được đăng ký. Nếu chưa kích hoạt, hãy vào trang kích hoạt để gửi lại mã.");
            AppUser u=new AppUser();u.setEmail(e);u.setUsername(name);u.setFullName(full);u.setPasswordHash(hash);u.setActive(false);em.persist(u);em.flush();issue(em,u,"ACTIVATE");return u;
        });
    }
    private String check(EntityManager em,AppUser u,String purpose,String code){
        OtpToken t=token(em,u,purpose);
        if(t==null||t.isUsed()||!t.getExpiresAt().isAfter(clock.instant())||t.getAttempts()>=5)return "Mã không hợp lệ, đã hết hạn hoặc đã dùng. Hãy yêu cầu mã mới.";
        t.setAttempts(t.getAttempts()+1);
        if(code==null||!code.matches("[0-9]{6}")||!PasswordHasher.verify(code,t.getCodeHash()))return "Mã OTP không đúng. Tối đa 5 lần thử cho mỗi mã.";
        t.setUsed(true);return null;
    }
    public void activate(String email,String code){String e=email(email);
        String error=Db.write(em->{AppUser u=byEmail(em,e);if(u==null)return "Mã hoặc email không hợp lệ";em.lock(u,LockModeType.PESSIMISTIC_WRITE);
            if(u.isActive())return "Tài khoản đã kích hoạt, hãy đăng nhập";
            String msg=check(em,u,"ACTIVATE",code);if(msg==null)u.setActive(true);return msg;});if(error!=null)throw new IllegalArgumentException(error);
    }
    public void requestOtp(String email,String purpose){String e=email(email);if(!Set.of("ACTIVATE","RESET").contains(purpose))throw new IllegalArgumentException("Yêu cầu không hợp lệ");
        Db.write(em->{AppUser u=byEmail(em,e);if(u==null)return null;em.lock(u,LockModeType.PESSIMISTIC_WRITE);
            if((purpose.equals("RESET")&&!u.isActive())||(purpose.equals("ACTIVATE")&&u.isActive()))return null;
            issue(em,u,purpose);return null;});
    }
    public void resetPassword(String email,String code,String newPassword){String e=email(email);password(newPassword);String hash=PasswordHasher.hash(newPassword);
        String error=Db.write(em->{AppUser u=byEmail(em,e);if(u==null||!u.isActive())return "Mã hoặc email không hợp lệ";em.lock(u,LockModeType.PESSIMISTIC_WRITE);
            String msg=check(em,u,"RESET",code);if(msg==null){u.setPasswordHash(hash);u.setSessionVersion(u.getSessionVersion()+1);u.setFailedLogins(0);u.setLockedUntil(null);}return msg;});
        if(error!=null)throw new IllegalArgumentException(error);
    }
    public AppUser login(String identity,String password){
        String key=identity==null?"":identity.trim().toLowerCase(Locale.ROOT);
        if(key.isBlank()||key.length()>254||password==null||password.isEmpty()||password.length()>128)throw new IllegalArgumentException("Thông tin đăng nhập không đúng");
        record Result(AppUser user,String error){}
        Result result=Db.write(em->{AppUser u=em.createQuery("SELECT u FROM AppUser u WHERE u.username=:key OR u.email=:key",AppUser.class).setParameter("key",key).getResultStream().findFirst().orElse(null);
            if(u==null)return new Result(null,"Thông tin đăng nhập không đúng");em.lock(u,LockModeType.PESSIMISTIC_WRITE);
            if(u.getLockedUntil()!=null&&u.getLockedUntil().isAfter(clock.instant()))return new Result(null,"Đăng nhập tạm khóa. Hãy thử lại sau 15 phút.");
            if(!PasswordHasher.verify(password,u.getPasswordHash())){u.setFailedLogins(u.getFailedLogins()+1);if(u.getFailedLogins()>=5){u.setLockedUntil(clock.instant().plusSeconds(900));u.setFailedLogins(0);}return new Result(null,"Thông tin đăng nhập không đúng");}
            if(!u.isActive())return new Result(null,"Tài khoản chưa kích hoạt. Hãy xác nhận OTP trong email.");
            u.setFailedLogins(0);u.setLockedUntil(null);return new Result(u,null);
        });if(result.error()!=null)throw new IllegalArgumentException(result.error());return result.user();
    }
    public static void profileFields(String name,String phone){
        if(name==null||name.trim().isEmpty()||name.trim().length()>100)throw new IllegalArgumentException("Họ tên cần từ 1 đến 100 ký tự");
        if(phone==null||!phone.trim().matches("[+]?[0-9]{9,15}"))throw new IllegalArgumentException("Số điện thoại cần từ 9 đến 15 chữ số, có thể bắt đầu bằng +");
    }
    public void updateProfile(long id,String name,String phone,String image){
        profileFields(name,phone);
        Db.write(em->{AppUser u=em.find(AppUser.class,id,LockModeType.PESSIMISTIC_WRITE);if(u==null||!u.isActive())throw new IllegalArgumentException("Tài khoản không hợp lệ");u.setFullName(name.trim());u.setPhone(phone.trim());if(image!=null)u.setImages(image);return null;});
    }
    public void requestEmailChange(long id,String target){String e=email(target);
        Db.write(em->{AppUser u=em.find(AppUser.class,id,LockModeType.PESSIMISTIC_WRITE);if(u==null||!u.isActive())throw new IllegalArgumentException("Tài khoản không hợp lệ");
            if(e.equals(u.getEmail()))throw new IllegalArgumentException("Email mới phải khác email hiện tại");
            if(byEmail(em,e)!=null)throw new IllegalArgumentException("Email đã được sử dụng");
            u.setPendingEmail(e);issue(em,u,"EMAIL_CHANGE");return null;});
    }
    public void confirmEmailChange(long id,String code){
        String error=Db.write(em->{AppUser u=em.find(AppUser.class,id,LockModeType.PESSIMISTIC_WRITE);
            if(u==null||!u.isActive()||u.getPendingEmail()==null)return "Không có yêu cầu đổi email";
            if(byEmail(em,u.getPendingEmail())!=null)return "Email đã được tài khoản khác sử dụng";
            String msg=check(em,u,"EMAIL_CHANGE",code);if(msg==null){u.setEmail(u.getPendingEmail());u.setPendingEmail(null);}return msg;});
        if(error!=null)throw new IllegalArgumentException(error);
    }
    public void cancelEmailChange(long id){Db.write(em->{AppUser u=em.find(AppUser.class,id,LockModeType.PESSIMISTIC_WRITE);u.setPendingEmail(null);OtpToken t=token(em,u,"EMAIL_CHANGE");if(t!=null)t.setUsed(true);return null;});}
    public AppUser find(Long id){return id==null?null:Db.read(em->em.find(AppUser.class,id));}
}
