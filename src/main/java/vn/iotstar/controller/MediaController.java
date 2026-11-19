package vn.iotstar.controller;
import jakarta.servlet.http.*;import java.io.*;import java.nio.file.*;import vn.iotstar.entity.*;import vn.iotstar.web.VideoStorage;import vn.iotstar.service.impl.VideoService;
public class MediaController extends HttpServlet {
    protected void doGet(HttpServletRequest r,HttpServletResponse s)throws IOException{serve(r,s,false);}
    protected void doHead(HttpServletRequest r,HttpServletResponse s)throws IOException{serve(r,s,true);}
    private void serve(HttpServletRequest r,HttpServletResponse s,boolean head)throws IOException{
        var service=new VideoService();Video v=service.find(r.getParameter("id"));var u=(AppUser)r.getAttribute("currentUser");
        if(!service.canView(v,u==null?null:u.getId())||!VideoStorage.local(v.getSource())){s.sendError(404);return;}
        Path p=VideoStorage.directory().resolve(v.getSource());if(!Files.isRegularFile(p)){s.sendError(404);return;}
        long size=Files.size(p),start=0,end=size-1;String range=r.getHeader("Range");
        s.setHeader("Accept-Ranges","bytes");String ext=v.getSource().substring(v.getSource().lastIndexOf('.')+1);s.setContentType("video/"+ext);s.setHeader("X-Content-Type-Options","nosniff");
        if(range!=null){try{if(!range.matches("bytes=[0-9]*-[0-9]*"))throw new IllegalArgumentException();String[] bits=range.substring(6).split("-",-1);
                if(bits[0].isEmpty()){long last=Long.parseLong(bits[1]);if(last<=0)throw new IllegalArgumentException();start=Math.max(0,size-last);}
                else{start=Long.parseLong(bits[0]);if(!bits[1].isEmpty())end=Math.min(end,Long.parseLong(bits[1]));}
                if(start<0||start>=size||end<start)throw new IllegalArgumentException();
            }catch(IllegalArgumentException e){s.setStatus(416);s.setHeader("Content-Range","bytes */"+size);return;}
            s.setStatus(206);s.setHeader("Content-Range","bytes "+start+"-"+end+"/"+size);
        }
        long count=end-start+1;s.setContentLengthLong(count);if(head)return;
        try(RandomAccessFile file=new RandomAccessFile(p.toFile(),"r")){file.seek(start);byte[] buffer=new byte[64*1024];var out=s.getOutputStream();while(count>0){int n=file.read(buffer,0,(int)Math.min(buffer.length,count));if(n<0)break;out.write(buffer,0,n);count-=n;}}
    }
}
