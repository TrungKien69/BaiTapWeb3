package vn.iotstar.web;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import vn.iotstar.entity.AppUser;
import vn.iotstar.service.impl.AuthService;
public class AuthFilter implements Filter {
    public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain)throws IOException,ServletException {
        var req=(HttpServletRequest)request;var resp=(HttpServletResponse)response;var session=req.getSession(false);AppUser user=null;
        if(session!=null&&session.getAttribute("userId") instanceof Long id){
            user=new AuthService().find(id);
            if(user==null||!user.isActive()||!Integer.valueOf(user.getSessionVersion()).equals(session.getAttribute("sessionVersion"))){session.removeAttribute("userId");session.removeAttribute("sessionVersion");user=null;}
        }
        req.setAttribute("currentUser",user);
        String path=req.getServletPath();boolean protectedPage=path.startsWith("/profile")||path.startsWith("/admin/")||path.equals("/video")||path.matches("/video/(add|edit|insert|update|delete)")||path.equals("/upload-demo")||path.equals("/multiPartServlet");
        if(protectedPage&&user==null){resp.sendRedirect(req.getContextPath()+"/login");return;}
        if(!path.startsWith("/assets/")&&!path.equals("/media/video"))resp.setHeader("Cache-Control","no-store");
        chain.doFilter(req,resp);
    }
}
