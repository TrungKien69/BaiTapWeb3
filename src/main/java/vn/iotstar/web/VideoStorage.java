package vn.iotstar.web;
import jakarta.servlet.http.Part;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import vn.iotstar.config.Settings;
public final class VideoStorage {
    public static final long MAX=100L*1024*1024;
    private VideoStorage(){}
    public static Path directory() throws IOException{return Files.createDirectories(Path.of(Settings.get("video.dir",Path.of(System.getProperty("user.home"),"jpa-category-videos").toString())));}
    public static boolean local(String name){return name!=null&&name.matches("[a-f0-9-]{36}[.](mp4|webm|ogg)");}
    public static String save(Part part) throws IOException {
        if(part.getSize()==0||part.getSize()>MAX)throw new IllegalArgumentException("Video phải có dữ liệu và không vượt quá 100 MB");
        Path temp=Files.createTempFile(directory(),"upload-",".part");
        try{
            // Multipart Servlet: Part.write dùng đường dẫn tuyệt đối do server tạo, không dùng tên gốc.
            part.write(temp.toAbsolutePath().toString());
            byte[] h;try(var in=Files.newInputStream(temp)){h=in.readNBytes(16);}
            String ext;
            if(h.length>=12&&new String(h,4,4,java.nio.charset.StandardCharsets.US_ASCII).equals("ftyp"))ext="mp4";
            else if(h.length>=4&&(h[0]&255)==0x1a&&(h[1]&255)==0x45&&(h[2]&255)==0xdf&&(h[3]&255)==0xa3)ext="webm";
            else if(h.length>=4&&new String(h,0,4,java.nio.charset.StandardCharsets.US_ASCII).equals("OggS"))ext="ogg";
            else throw new IllegalArgumentException("Chỉ nhận file video MP4, WebM hoặc OGG đúng định dạng");
            String name=UUID.randomUUID()+"."+ext;Files.move(temp,directory().resolve(name));return name;
        }finally{Files.deleteIfExists(temp);}
    }
    public static void delete(String name) throws IOException {if(local(name))Files.deleteIfExists(directory().resolve(name));}
    public static String url(String value){
        if(value==null||value.isBlank())return null;
        String s=value.trim();try{var u=java.net.URI.create(s);if(s.length()>1000||u.getHost()==null||!Set.of("http","https").contains(u.getScheme().toLowerCase(Locale.ROOT)))throw new IllegalArgumentException();return s;}
        catch(RuntimeException e){throw new IllegalArgumentException("Link video phải là HTTP/HTTPS trực tiếp, tối đa 1000 ký tự");}
    }
}
