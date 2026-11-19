package vn.iotstar.entity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
@Entity(name="AppUser") @Table(name="users")
public class AppUser {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,unique=true,length=32) private String username;
    @Column(nullable=false,unique=true,length=254) private String email;
    @Column(name="full_name",nullable=false,length=100) private String fullName;
    @Column(name="password_hash",nullable=false,length=255) private String passwordHash;
    @Column(nullable=false) private boolean active;
    @Column(name="session_version",nullable=false) private int sessionVersion;
    @Column(name="failed_logins",nullable=false) private int failedLogins;
    private Instant lockedUntil;
    @Column(name="created_at",nullable=false) private Instant createdAt=Instant.now();
    @OneToMany(mappedBy="owner") private List<Video> videos=new ArrayList<>();
    @Column(length=20) private String phone;
    @Column(length=1000) private String images;
    @Column(length=254) private String pendingEmail;
    public String getPhone(){return phone;} public void setPhone(String s){phone=s;}
    public String getImages(){return images;} public void setImages(String s){images=s;}
    public String getPendingEmail(){return pendingEmail;} public void setPendingEmail(String s){pendingEmail=s;}
    public AppUser(){}
    public Long getId(){return id;} public String getUsername(){return username;} public void setUsername(String v){username=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
    public String getPasswordHash(){return passwordHash;} public void setPasswordHash(String v){passwordHash=v;}
    public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
    public int getSessionVersion(){return sessionVersion;} public void setSessionVersion(int v){sessionVersion=v;}
    public int getFailedLogins(){return failedLogins;} public void setFailedLogins(int v){failedLogins=v;}
    public Instant getLockedUntil(){return lockedUntil;} public void setLockedUntil(Instant v){lockedUntil=v;}
    public Instant getCreatedAt(){return createdAt;} public List<Video> getVideos(){return videos;}
}
