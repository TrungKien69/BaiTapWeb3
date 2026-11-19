package vn.iotstar.service.impl;
import vn.iotstar.config.Db;
import vn.iotstar.entity.*;
import java.util.*;
import java.time.Instant;
public class VideoService {
    public record VideoPage(List<Video> videos,int page,int pages,long total){}
    private static final String PUBLIC="v.active=1 AND v.owner IS NOT NULL AND v.owner.active=true";
    public List<Video> latest(){return Db.read(em->em.createQuery("SELECT v FROM Video v JOIN FETCH v.owner WHERE "+PUBLIC+" ORDER BY v.createdAt DESC, v.videoId DESC",Video.class).setMaxResults(10).getResultList());}
    public VideoPage byUser(long user,int page){return Db.read(em->{
        long total=em.createQuery("SELECT COUNT(v) FROM Video v WHERE v.owner.id=:u",Long.class).setParameter("u",user).getSingleResult();
        int pages=Math.max(1,(int)((total+5)/6)),p=Math.min(Math.max(1,page),pages);
        var list=em.createQuery("SELECT v FROM Video v JOIN FETCH v.owner WHERE v.owner.id=:u ORDER BY v.createdAt DESC, v.videoId DESC",Video.class).setParameter("u",user).setFirstResult((p-1)*6).setMaxResults(6).getResultList();return new VideoPage(list,p,pages,total);
    });}
    public Video find(String id){if(id==null||id.length()>50)return null;return Db.read(em->em.createQuery("SELECT v FROM Video v LEFT JOIN FETCH v.owner LEFT JOIN FETCH v.category WHERE v.videoId=:id",Video.class).setParameter("id",id).getResultStream().findFirst().orElse(null));}
    public boolean canView(Video v,Long user){return v!=null&&v.getOwner()!=null&&((user!=null&&v.getOwner().getId().equals(user))||(v.getActive()==1&&v.getOwner().isActive()));}
    public Video owned(String id,long user){Video v=find(id);if(v==null||v.getOwner()==null||v.getOwner().getId()!=user)throw new NoSuchElementException("Không tìm thấy video của bạn");return v;}
    private void validate(Video v){String title=v.getTitle()==null?"":v.getTitle().trim();if(title.isBlank()||title.length()>200)throw new IllegalArgumentException("Tiêu đề cần từ 1 đến 200 ký tự");v.setTitle(title);
        if(v.getDescription()!=null&&v.getDescription().length()>500)throw new IllegalArgumentException("Mô tả tối đa 500 ký tự");
        if(v.getActive()!=0&&v.getActive()!=1)throw new IllegalArgumentException("Trạng thái không hợp lệ");
        if(v.getSource()==null||v.getSource().isBlank())throw new IllegalArgumentException("Vui lòng tải video lên hoặc nhập link video trực tiếp");
    }
    public void save(Video input,long user,boolean edit){validate(input);Db.write(em->{
        AppUser owner=em.find(AppUser.class,user);if(owner==null||!owner.isActive())throw new IllegalArgumentException("Tài khoản chưa kích hoạt");
        Video v;
        if(edit){v=em.find(Video.class,input.getVideoId());if(v==null||v.getOwner()==null||v.getOwner().getId()!=user)throw new NoSuchElementException("Không tìm thấy video của bạn");}
        else{v=new Video();v.setVideoId(UUID.randomUUID().toString());v.setOwner(owner);v.setCreatedAt(Instant.now());}
        v.setTitle(input.getTitle());v.setDescription(input.getDescription());v.setActive(input.getActive());v.setSource(input.getSource());v.setPoster(input.getPoster());
        if(input.getCategory()!=null){Category c=em.find(Category.class,input.getCategory().getCategoryid());if(c==null)throw new IllegalArgumentException("Danh mục không tồn tại");v.setCategory(c);}else v.setCategory(null);
        if(!edit)em.persist(v);input.setVideoId(v.getVideoId());return null;
    });}
    public Video delete(String id,long user){return Db.write(em->{Video v=em.find(Video.class,id);if(v==null||v.getOwner()==null||v.getOwner().getId()!=user)throw new NoSuchElementException("Không tìm thấy video của bạn");em.remove(v);return v;});}
    public void viewed(String id){Db.write(em->{em.createQuery("UPDATE Video v SET v.views=v.views+1 WHERE v.videoId=:id").setParameter("id",id).executeUpdate();return null;});}
}
