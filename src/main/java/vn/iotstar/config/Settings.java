package vn.iotstar.config;
import java.util.*;
import java.nio.file.*;
public final class Settings {
    private Settings() {}
    public static String get(String key,String fallback) {
        String s=System.getProperty(key); if(s==null)s=System.getenv(key.toUpperCase(Locale.ROOT).replace('.','_'));
        if(s==null){var p=new Properties();Path f=Path.of(System.getProperty("mail.config","config/mail.properties"));
            if(Files.isRegularFile(f))try(var in=Files.newBufferedReader(f)){p.load(in);}catch(java.io.IOException e){throw new IllegalStateException("Không đọc được cấu hình email",e);}
            s=p.getProperty(key);
        }
        return s==null?fallback:s;
    }
    public static boolean demoMail(){return Boolean.parseBoolean(get("app.demo","false"))&&"file".equals(get("mail.mode","smtp"));}
}
