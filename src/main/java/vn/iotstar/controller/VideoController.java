package vn.iotstar.controller;
import jakarta.servlet.*;import jakarta.servlet.http.*;import jakarta.servlet.annotation.MultipartConfig;
import java.io.*;import java.util.*;
import vn.iotstar.entity.*;import vn.iotstar.service.impl.*;import vn.iotstar.web.*;
@MultipartConfig(fileSizeThreshold=1024*1024,maxFileSize=104857600,maxRequestSize=107000000)
public class VideoController extends HttpServlet {
    private final VideoService service=new VideoService();
    private Long user(HttpServletRequest r){var u=(AppUser)r.getAttribute("currentUser");return u==null?null:u.getId();}
    private String value(HttpServletRequest r,String key){String v=r.getParameter(key);return v==null?"":v;}
    private void view(HttpServletRequest r,HttpServletResponse s,String file)throws ServletException,IOException{
        Object flash=r.getSession().getAttribute("flash");r.setAttribute("flash",flash);r.getSession().removeAttribute("flash");r.getRequestDispatcher("/WEB-INF/views/"+file+".jsp").forward(r,s);
    }
    private void form(HttpServletRequest r,HttpServletResponse s,Video v,boolean edit)throws ServletException,IOException{r.setAttribute("video",v);r.setAttribute("edit",edit);r.setAttribute("categories",new CategoryServiceImpl().findAll());view(r,s,"video-form");}
    protected void doGet(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
        String path=r.getServletPath();try{
            switch(path){
                case "/home"->{r.setAttribute("videos",service.latest());view(r,s,"home");}
                case "/video"->{int p=1;try{p=Integer.parseInt(r.getParameter("page"));}catch(NumberFormatException ignored){}var list=service.byUser(user(r),p);r.setAttribute("listing",list);r.setAttribute("videos",list.videos());r.setAttribute("page",list.page());r.setAttribute("pages",list.pages());r.setAttribute("total",list.total());view(r,s,"videos");}
                case "/video/add","/upload-demo","/multiPartServlet"->{r.setAttribute("uploadDemo",!path.equals("/video/add"));Video v=new Video();v.setActive(1);form(r,s,v,false);}
                case "/video/edit"->form(r,s,service.owned(value(r,"id"),user(r)),true);
                case "/video/detail"->{Video v=service.find(value(r,"id"));if(!service.canView(v,user(r))){s.sendError(404);return;}service.viewed(v.getVideoId());v.setViews(v.getViews()+1);r.setAttribute("video",v);r.setAttribute("owner",user(r)!=null&&v.getOwner().getId().equals(user(r)));view(r,s,"video-detail");}
                default->s.sendError(405);
            }
        }catch(NoSuchElementException e){s.sendError(404);}
    }
    protected void doPost(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
        String path=r.getServletPath();boolean edit=path.equals("/video/update");Video input=new Video();String oldSource=null,oldPoster=null,newSource=null,newPoster=null;
        if(path.equals("/video/delete")){
            try{Video v=service.delete(value(r,"id"),user(r));cleanup(v.getSource(),v.getPoster());r.getSession().setAttribute("flash","Đã xóa video");s.sendRedirect(r.getContextPath()+"/video");}catch(NoSuchElementException e){s.sendError(404);}return;
        }
        if(!edit&&!Set.of("/video/insert","/multiPartServlet").contains(path)){s.sendError(405);return;}
        try{
            if(edit){Video old=service.owned(value(r,"id"),user(r));input.setVideoId(old.getVideoId());oldSource=old.getSource();oldPoster=old.getPoster();}
            input.setSource(oldSource);input.setPoster(oldPoster);input.setTitle(value(r,"title"));input.setDescription(value(r,"description"));
            String active=value(r,"active");if(!Set.of("0","1").contains(active))throw new IllegalArgumentException("Trạng thái không hợp lệ");input.setActive(Integer.parseInt(active));
            String cat=value(r,"categoryid");if(!cat.isBlank()){Category c=new Category();try{c.setCategoryid(Integer.parseInt(cat));}catch(NumberFormatException ex){throw new IllegalArgumentException("Mã danh mục không hợp lệ");}input.setCategory(c);}
            Part video=r.getPart("videoFile");if(video==null||video.getSize()==0)video=r.getPart("multiPartServlet");
            if(video!=null&&video.getSize()>0){newSource=VideoStorage.save(video);input.setSource(newSource);}else{String url=VideoStorage.url(value(r,"source"));if(url!=null)input.setSource(url);}
            Part poster=r.getPart("posterFile");if(poster!=null&&poster.getSize()>0){newPoster=ImageStorage.save(poster);input.setPoster(newPoster);}else if("1".equals(value(r,"removePoster")))input.setPoster(null);else{String url=ImageStorage.validUrl(value(r,"poster"));if(url!=null)input.setPoster(url);}
            service.save(input,user(r),edit);
        }catch(NoSuchElementException e){cleanup(newSource,newPoster);s.sendError(404);return;}
        catch(IllegalArgumentException|IllegalStateException e){cleanup(newSource,newPoster);input.setSource(oldSource);input.setPoster(oldPoster);s.setStatus(400);r.setAttribute("error",e.getMessage());form(r,s,input,edit);return;}
        catch(IOException|ServletException e){cleanup(newSource,newPoster);throw e;}
        catch(RuntimeException e){cleanup(newSource,newPoster);log("Lưu video thất bại",e);s.setStatus(400);r.setAttribute("error","Không lưu được video. Hãy kiểm tra dữ liệu và kết nối DB.");input.setSource(oldSource);input.setPoster(oldPoster);form(r,s,input,edit);return;}
        if(oldSource!=null&&!oldSource.equals(input.getSource()))cleanup(oldSource,null);if(oldPoster!=null&&!oldPoster.equals(input.getPoster()))cleanup(null,oldPoster);
        r.getSession().setAttribute("flash",edit?"Đã cập nhật video":"Đã thêm video");s.sendRedirect(r.getContextPath()+"/video");
    }
    private void cleanup(String source,String poster){try{VideoStorage.delete(source);}catch(IOException e){log("Không xóa được video cũ",e);}try{ImageStorage.delete(poster);}catch(IOException e){log("Không xóa được poster cũ",e);}}
}
