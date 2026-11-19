package vn.iotstar.service.impl;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.servlet.http.Part;
import java.io.*;import java.nio.file.*;import java.util.*;import javax.imageio.ImageIO;
public class CloudinaryImages {
    private static String config(String key){String v=System.getProperty("cloudinary."+key);if(v==null)v=System.getenv("CLOUDINARY_"+key.toUpperCase(Locale.ROOT));
        if(v==null){Properties p=new Properties();Path f=Path.of("config/cloudinary.properties");if(Files.isRegularFile(f))try(var in=Files.newBufferedReader(f)){p.load(in);}catch(IOException e){throw new IllegalStateException("Không đọc được cấu hình Cloudinary");}v=p.getProperty("cloudinary."+key);}
        if(v==null||v.isBlank()||v.startsWith("THAY_"))throw new IllegalStateException("Hãy cấu hình config/cloudinary.properties trước khi upload ảnh");return v;}
    public String upload(Part part)throws IOException{
        if(part==null||part.getSize()==0)return null;
        if(part.getSize()>2*1024*1024)throw new IllegalArgumentException("Ảnh tối đa 2 MB");
        byte[] bytes;try(var in=part.getInputStream()){bytes=in.readNBytes(2*1024*1024+1);}
        if(bytes.length>2*1024*1024)throw new IllegalArgumentException("Ảnh tối đa 2 MB");
        try(var input=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))){
            var readers=ImageIO.getImageReaders(input);if(!readers.hasNext())throw new IllegalArgumentException("Chỉ nhận ảnh PNG hoặc JPEG hợp lệ");
            var reader=readers.next();try{String format=reader.getFormatName();if(!format.equalsIgnoreCase("PNG")&&!format.equalsIgnoreCase("JPEG"))throw new IllegalArgumentException("Chỉ nhận ảnh PNG hoặc JPEG");reader.setInput(input);int w=reader.getWidth(0),h=reader.getHeight(0);if(w<=0||h<=0||(long)w*h>16000000)throw new IllegalArgumentException("Ảnh tối đa 16 triệu điểm ảnh");if(reader.read(0)==null)throw new IllegalArgumentException("Ảnh bị hỏng");}finally{reader.dispose();}}
        Cloudinary cloud=new Cloudinary(ObjectUtils.asMap("cloud_name",config("cloud_name"),"api_key",config("api_key"),"api_secret",config("api_secret"),"secure",true));
        try{var result=cloud.uploader().upload(bytes,ObjectUtils.asMap("folder","videohub/avatars","resource_type","image","allowed_formats",List.of("png","jpg","jpeg")));String url=(String)result.get("secure_url");if(url==null||!url.startsWith("https://"))throw new IOException("Cloudinary không trả về URL hợp lệ");return url;}
        catch(Exception e){throw new IOException("Upload Cloudinary thất bại. Hãy kiểm tra cấu hình và kết nối mạng.",e);}
    }
}
