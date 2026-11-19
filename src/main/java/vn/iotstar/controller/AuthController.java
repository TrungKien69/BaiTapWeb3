package vn.iotstar.controller;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import vn.iotstar.service.impl.AuthService;
import vn.iotstar.config.Settings;
public class AuthController extends HttpServlet {
    private final AuthService auth=new AuthService();
    private String mode(HttpServletRequest req){return switch(req.getServletPath()){case "/register"->"register";case "/activate"->"activate";case "/forgot-password"->"forgot";case "/reset-password"->"reset";default->"login";};}
    private String val(HttpServletRequest r,String key){String s=r.getParameter(key);return s==null?"":s;}
    private void form(HttpServletRequest r,HttpServletResponse s,String mode)throws ServletException,IOException{
        r.setAttribute("mode",mode);r.setAttribute("demoMail",Settings.demoMail());
        Object flash=r.getSession().getAttribute("flash");r.setAttribute("flash",flash);r.getSession().removeAttribute("flash");
        if(r.getAttribute("email")==null)r.setAttribute("email",r.getSession().getAttribute("pendingEmail"));
        r.getRequestDispatcher("/WEB-INF/views/auth.jsp").forward(r,s);
    }
    protected void doGet(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
        if(r.getServletPath().equals("/logout")||r.getServletPath().equals("/otp/resend")){s.sendError(405);return;}form(r,s,mode(r));
    }
    private void redirect(HttpServletRequest r,HttpServletResponse s,String path)throws IOException{s.sendRedirect(r.getContextPath()+path);}
    private void pending(HttpServletRequest r,String email,String purpose){r.getSession().setAttribute("pendingEmail",AuthService.email(email));r.getSession().setAttribute("pendingPurpose",purpose);}
    protected void doPost(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
        String path=r.getServletPath(),m=mode(r);String email=val(r,"email");r.setAttribute("email",email);
        try{
            switch(path){
                case "/register"->{if(!val(r,"password").equals(val(r,"confirm")))throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");auth.register(val(r,"username"),email,val(r,"fullName"),val(r,"password"));pending(r,email,"ACTIVATE");r.getSession().setAttribute("flash","Đăng ký thành công. Hãy nhập OTP để kích hoạt tài khoản.");redirect(r,s,"/activate");}
                case "/activate"->{auth.activate(email,val(r,"otp"));r.getSession().setAttribute("flash","Kích hoạt thành công. Bạn có thể đăng nhập.");redirect(r,s,"/login");}
                case "/login"->{var u=auth.login(val(r,"identity"),val(r,"password"));r.changeSessionId();r.getSession().setAttribute("userId",u.getId());r.getSession().setAttribute("sessionVersion",u.getSessionVersion());r.getSession().removeAttribute("pendingEmail");r.getSession().removeAttribute("pendingPurpose");redirect(r,s,"/home");}
                case "/logout"->{r.getSession().invalidate();redirect(r,s,"/login");}
                case "/forgot-password"->{auth.requestOtp(email,"RESET");pending(r,email,"RESET");r.getSession().setAttribute("flash","Nếu email thuộc tài khoản đã kích hoạt, mã xác nhận đã được gửi.");redirect(r,s,"/reset-password");}
                case "/reset-password"->{if(!val(r,"password").equals(val(r,"confirm")))throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");auth.resetPassword(email,val(r,"otp"),val(r,"password"));r.getSession().removeAttribute("userId");r.getSession().removeAttribute("sessionVersion");r.getSession().setAttribute("flash","Đổi mật khẩu thành công. Hãy đăng nhập bằng mật khẩu mới.");redirect(r,s,"/login");}
                case "/otp/resend"->{String purpose=val(r,"purpose");m="RESET".equals(purpose)?"reset":"activate";auth.requestOtp(email,purpose);pending(r,email,purpose);r.getSession().setAttribute("flash","Nếu tài khoản phù hợp, mã mới đã được gửi. Mã cũ không còn hiệu lực.");redirect(r,s,"RESET".equals(purpose)?"/reset-password":"/activate");}
                default->s.sendError(404);
            }
        }catch(IllegalArgumentException|IllegalStateException e){s.setStatus(400);r.setAttribute("error",e.getMessage());form(r,s,m);}
        catch(RuntimeException e){log("Xử lý tài khoản thất bại",e);s.setStatus(400);r.setAttribute("error","Không xử lý được yêu cầu. Kiểm tra dữ liệu hoặc cấu hình email.");form(r,s,m);}
    }
}
