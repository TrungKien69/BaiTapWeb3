package vn.iotstar.controller;
import jakarta.servlet.*;import jakarta.servlet.http.*;import java.io.*;import java.nio.file.*;import java.util.*;
import vn.iotstar.config.Settings;import vn.iotstar.service.impl.MailService;
public class DemoMailController extends HttpServlet {
    protected void doGet(HttpServletRequest r,HttpServletResponse s)throws IOException,ServletException{
        if(!Settings.demoMail()){s.sendError(404);return;}
        String email=(String)r.getSession().getAttribute("pendingEmail");List<String> messages=new ArrayList<>();
        if(email!=null&&Files.isDirectory(MailService.outbox()))try(var files=Files.list(MailService.outbox())){
            for(Path p:files.filter(f->f.getFileName().toString().endsWith(".eml")).sorted(Comparator.reverseOrder()).limit(200).toList()){
                String text=Files.readString(p);if(text.startsWith("To: "+email+"\n"))messages.add(text);if(messages.size()==10)break;
            }
        }
        r.setAttribute("messages",messages);r.getRequestDispatcher("/WEB-INF/views/demo-mail.jsp").forward(r,s);
    }
}
