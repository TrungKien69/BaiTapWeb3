package vn.iotstar.security;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.*;
import java.util.*;
public final class PasswordHasher {
    private static final int ITERATIONS=600000;
    private static final SecureRandom RANDOM=new SecureRandom();
    private PasswordHasher(){}
    public static String hash(String password) {
        byte[] salt=new byte[16];RANDOM.nextBytes(salt);
        return "pbkdf2_sha256$"+ITERATIONS+"$"+Base64.getEncoder().encodeToString(salt)+"$"+Base64.getEncoder().encodeToString(derive(password,salt,ITERATIONS));
    }
    private static byte[] derive(String password,byte[] salt,int iterations) {
        PBEKeySpec spec=new PBEKeySpec(password.toCharArray(),salt,iterations,256);
        try{return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();}
        catch(GeneralSecurityException e){throw new IllegalStateException(e);}finally{spec.clearPassword();}
    }
    public static boolean verify(String password,String stored) {
        if(password==null||stored==null||password.length()>128)return false;
        try{String[] p=stored.split("\\$");if(p.length!=4||!p[0].equals("pbkdf2_sha256"))return false;
            int n=Integer.parseInt(p[1]);if(n<100000||n>2000000)return false;
            return MessageDigest.isEqual(derive(password,Base64.getDecoder().decode(p[2]),n),Base64.getDecoder().decode(p[3]));
        }catch(IllegalArgumentException e){return false;}
    }
    public static String otp(){return String.format(Locale.ROOT,"%06d",RANDOM.nextInt(1000000));}
}
