package vn.iotstar.controller;
import jakarta.servlet.*;import jakarta.servlet.http.*;import java.io.*;
import vn.iotstar.entity.AppUser;import vn.iotstar.service.impl.*;
public class ProfileController extends HttpServlet {
    private final AuthService auth=new AuthService();private final CloudinaryImages images=new CloudinaryImages();
    private long id(HttpServletRequest r){return ((AppUser)r.getAttribute("currentUser")).getId();}
    private String val(HttpServletRequest r,String k){String s=r.getParameter(k);return s==null?"":s;}
    private void show(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{r.setAttribute("profile",auth.find(id(r)));r.setAttribute("demoProfileMail",vn.iotstar.config.Settings.demoMail());Object flash=r.getSession().getAttribute("flash");r.getSession().removeAttribute("flash");r.setAttribute("flash",flash);r.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(r,s);}
    protected void doGet(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{if(!r.getServletPath().equals("/profile")){s.sendError(405);return;}show(r,s);}
    protected void doPost(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
        try{switch(r.getServletPath()){
            case "/profile/update"->{AuthService.profileFields(val(r,"fullName"),val(r,"phone"));String url=images.upload(r.getPart("images"));auth.updateProfile(id(r),val(r,"fullName"),val(r,"phone"),url);r.getSession().setAttribute("flash","Đã cập nhật họ tên, số điện thoại và ảnh hồ sơ.");}
            case "/profile/email/request"->{auth.requestEmailChange(id(r),val(r,"email"));r.getSession().setAttribute("pendingEmail",AuthService.email(val(r,"email")));r.getSession().setAttribute("flash","OTP đã gửi đến email mới. Email hiện tại chưa thay đổi.");}
            case "/profile/email/confirm"->{auth.confirmEmailChange(id(r),val(r,"otp"));r.getSession().removeAttribute("pendingEmail");r.getSession().setAttribute("flash","Đổi email thành công.");}
            case "/profile/email/cancel"->{auth.cancelEmailChange(id(r));r.getSession().removeAttribute("pendingEmail");r.getSession().setAttribute("flash","Đã hủy yêu cầu đổi email.");}
            default->{s.sendError(404);return;}}
            s.sendRedirect(r.getContextPath()+"/profile");
        }catch(IllegalArgumentException|IllegalStateException e){s.setStatus(400);r.setAttribute("error",e.getMessage());show(r,s);}
        catch(IOException e){log("Upload avatar failed",e);s.setStatus(400);r.setAttribute("error","Không upload được ảnh. Kiểm tra cấu hình Cloudinary và file ảnh.");show(r,s);}
        catch(RuntimeException e){log("Profile failed",e);s.setStatus(400);r.setAttribute("error","Không cập nhật được hồ sơ. Email có thể đã được sử dụng; hãy thử lại.");show(r,s);}
    }
}
