package vn.iotstar.entity;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="otp_tokens",uniqueConstraints=@UniqueConstraint(columnNames={"user_id","purpose"}))
public class OtpToken {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="user_id",nullable=false) private AppUser user;
    @Column(nullable=false,length=12) private String purpose;
    @Column(name="code_hash",nullable=false,length=255) private String codeHash;
    @Column(nullable=false) private Instant expiresAt;
    @Column(nullable=false) private Instant sentAt;
    @Column(nullable=false) private int attempts;
    @Column(nullable=false) private boolean used;
    public OtpToken(){}
    public AppUser getUser(){return user;} public void setUser(AppUser v){user=v;}
    public String getPurpose(){return purpose;} public void setPurpose(String v){purpose=v;}
    public String getCodeHash(){return codeHash;} public void setCodeHash(String v){codeHash=v;}
    public Instant getExpiresAt(){return expiresAt;} public void setExpiresAt(Instant v){expiresAt=v;}
    public Instant getSentAt(){return sentAt;} public void setSentAt(Instant v){sentAt=v;}
    public int getAttempts(){return attempts;} public void setAttempts(int v){attempts=v;}
    public boolean isUsed(){return used;} public void setUsed(boolean v){used=v;}
}
