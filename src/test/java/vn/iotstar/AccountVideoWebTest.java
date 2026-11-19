package vn.iotstar;
import org.junit.jupiter.api.*;import static org.junit.jupiter.api.Assertions.*;
import java.net.*;import java.net.http.*;import java.nio.file.*;import java.nio.charset.StandardCharsets;import java.util.*;import java.util.regex.*;import java.io.*;
import org.apache.catalina.startup.Tomcat;
import vn.iotstar.config.*;import vn.iotstar.dev.DevServer;import vn.iotstar.entity.*;import vn.iotstar.security.PasswordHasher;import vn.iotstar.service.impl.*;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountVideoWebTest {
    Tomcat server;String base;Path outbox,media,images;HttpClient client;
    static final String PASSWORD="StrongPass9!",HASH=PasswordHasher.hash(PASSWORD);
    @BeforeAll void start()throws Exception{
        outbox=Files.createTempDirectory("account-mail-");media=Files.createTempDirectory("account-video-");images=Files.createTempDirectory("account-image-");
        System.setProperty("app.demo","true");System.setProperty("mail.mode","file");System.setProperty("mail.outbox",outbox.toString());System.setProperty("video.dir",media.toString());System.setProperty("upload.dir",images.toString());
        server=DevServer.start(0,Files.createTempDirectory("account-server-"));base="http://127.0.0.1:"+server.getConnector().getLocalPort()+"/jpa-category";
    }
    @AfterAll void stop()throws Exception{if(server!=null){server.stop();server.destroy();}for(String k:List.of("app.demo","mail.mode","mail.outbox","video.dir","upload.dir"))System.clearProperty(k);JpaConfig.close();}
    @BeforeEach void reset()throws Exception{
        Db.write(em->{em.createQuery("DELETE FROM Video").executeUpdate();em.createQuery("DELETE FROM OtpToken").executeUpdate();em.createQuery("DELETE FROM AppUser").executeUpdate();em.createQuery("DELETE FROM Category").executeUpdate();return null;});
        client=newClient();for(Path dir:List.of(outbox,media,images))try(var list=Files.list(dir)){for(Path p:list.toList())Files.delete(p);}
    }
    HttpClient newClient(){CookieManager c=new CookieManager();c.setCookiePolicy(CookiePolicy.ACCEPT_ALL);return HttpClient.newBuilder().cookieHandler(c).build();}
    HttpResponse<String> get(String path)throws Exception{return client.send(HttpRequest.newBuilder(URI.create(base+path)).GET().build(),HttpResponse.BodyHandlers.ofString());}
    String token()throws Exception{var page=get("/login");Matcher m=Pattern.compile("name=\"csrf\" value=\"([^\"]+)\"").matcher(page.body());assertTrue(m.find(),"status="+page.statusCode()+" headers="+page.headers()+" body="+page.body());return m.group(1);}
    HttpResponse<String> post(String path,Map<String,String> fields)throws Exception{
        Map<String,String> f=new LinkedHashMap<>(fields);f.putIfAbsent("csrf",token());String body=f.entrySet().stream().map(e->URLEncoder.encode(e.getKey(),StandardCharsets.UTF_8)+"="+URLEncoder.encode(e.getValue(),StandardCharsets.UTF_8)).collect(java.util.stream.Collectors.joining("&"));
        return client.send(HttpRequest.newBuilder(URI.create(base+path)).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
    }
    HttpResponse<String> multipart(String path,Map<String,String> fields,Map<String,byte[]> files)throws Exception{
        Map<String,String> f=new LinkedHashMap<>(fields);f.put("csrf",token());String boundary="----AccountVideo"+UUID.randomUUID();ByteArrayOutputStream out=new ByteArrayOutputStream();
        for(var e:f.entrySet())out.write(("--"+boundary+"\r\nContent-Disposition: form-data; name=\""+e.getKey()+"\"\r\n\r\n"+e.getValue()+"\r\n").getBytes(StandardCharsets.UTF_8));
        for(var e:files.entrySet()){out.write(("--"+boundary+"\r\nContent-Disposition: form-data; name=\""+e.getKey()+"\"; filename=\"sample.mp4\"\r\nContent-Type: application/octet-stream\r\n\r\n").getBytes());out.write(e.getValue());out.write("\r\n".getBytes());}out.write(("--"+boundary+"--\r\n").getBytes());
        return client.send(HttpRequest.newBuilder(URI.create(base+path)).header("Content-Type","multipart/form-data; boundary="+boundary).POST(HttpRequest.BodyPublishers.ofByteArray(out.toByteArray())).build(),HttpResponse.BodyHandlers.ofString());
    }
    AppUser account(String name){return Db.write(em->{AppUser u=new AppUser();u.setUsername(name);u.setEmail(name+"@example.test");u.setFullName("Người dùng "+name);u.setActive(true);u.setPasswordHash(HASH);em.persist(u);return u;});}
    void login(String name)throws Exception{assertEquals(302,post("/login",Map.of("identity",name,"password",PASSWORD)).statusCode());}
    void register()throws Exception{var res=post("/register",Map.of("username","alice","email","alice@example.test","fullName","Người dùng thử","password",PASSWORD,"confirm",PASSWORD));assertEquals(302,res.statusCode(),res.body());}
    String otp()throws Exception{String html=get("/demo/mail").body();Matcher m=Pattern.compile("Mã OTP của bạn: ([0-9]{6})").matcher(html);assertTrue(m.find(),html);return m.group(1);}
    Video seed(AppUser u,String title,boolean active){Video v=new Video();v.setTitle(title);v.setActive(active?1:0);v.setSource("https://example.com/test.mp4");new VideoService().save(v,u.getId(),false);return new VideoService().find(v.getVideoId());}
    int cards(String html){return html.split("class=\"panel videocard\"",-1).length-1;}
    @Test void registrationActivationLoginLogoutThroughForms()throws Exception{
        assertEquals(302,get("/video").statusCode());register();assertEquals(400,post("/login",Map.of("identity","alice","password",PASSWORD)).statusCode());String code=otp();
        assertEquals(400,post("/activate",Map.of("email","alice@example.test","otp","wrong")).statusCode());assertEquals(302,post("/activate",Map.of("email","alice@example.test","otp",code)).statusCode());
        login("alice");assertEquals(200,get("/video").statusCode());assertEquals(200,get("/admin/categories").statusCode());assertEquals(302,post("/logout",Map.of()).statusCode());assertEquals(302,get("/video").statusCode());
    }
    @Test void forgottenPasswordOtpChangesLoginAndInvalidatesOldSession()throws Exception{
        register();String code=otp();post("/activate",Map.of("email","alice@example.test","otp",code));login("alice");HttpClient old=client;client=newClient();
        assertEquals(302,post("/forgot-password",Map.of("email","alice@example.test")).statusCode());String reset=otp();assertEquals(400,post("/reset-password",Map.of("email","alice@example.test","otp","0000000","password","ChangedPass9!","confirm","ChangedPass9!")).statusCode());
        assertEquals(302,post("/reset-password",Map.of("email","alice@example.test","otp",reset,"password","ChangedPass9!","confirm","ChangedPass9!")).statusCode());
        assertEquals(400,post("/login",Map.of("identity","alice","password",PASSWORD)).statusCode());assertEquals(302,post("/login",Map.of("identity","alice","password","ChangedPass9!")).statusCode());
        var res=old.send(HttpRequest.newBuilder(URI.create(base+"/video")).GET().build(),HttpResponse.BodyHandlers.ofString());assertEquals(302,res.statusCode());
    }
    @Test void videoUploadReadEditDeleteAndByteRange()throws Exception{
        var u=account("alice");login("alice");byte[] movie=Files.readAllBytes(Path.of("samples/demo-video.mp4"));
        var res=multipart("/video/insert",Map.of("title","Video tiếng Việt","description","Mô tả thử","active","1"),Map.of("videoFile",movie));assertEquals(302,res.statusCode(),res.body());
        Video v=new VideoService().byUser(u.getId(),1).videos().get(0);assertTrue(Files.exists(media.resolve(v.getSource())));
        var detail=get("/video/detail?id="+v.getVideoId());assertEquals(200,detail.statusCode(),detail.body());assertTrue(detail.body().contains("<video"));assertTrue(detail.body().contains("Video tiếng Việt"));
        var bytes=client.send(HttpRequest.newBuilder(URI.create(base+"/media/video?id="+v.getVideoId())).header("Range","bytes=0-31").GET().build(),HttpResponse.BodyHandlers.ofByteArray());assertEquals(206,bytes.statusCode());assertArrayEquals(Arrays.copyOf(movie,32),bytes.body());
        var full=client.send(HttpRequest.newBuilder(URI.create(base+"/media/video?id="+v.getVideoId())).GET().build(),HttpResponse.BodyHandlers.ofByteArray());assertArrayEquals(movie,full.body());
        assertEquals(416,client.send(HttpRequest.newBuilder(URI.create(base+"/media/video?id="+v.getVideoId())).header("Range","bytes=999999999-").GET().build(),HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(302,multipart("/video/update",Map.of("id",v.getVideoId(),"title","Đã sửa","active","0"),Map.of()).statusCode());assertEquals("Đã sửa",new VideoService().find(v.getVideoId()).getTitle());assertEquals(v.getSource(),new VideoService().find(v.getVideoId()).getSource());
        assertEquals(302,post("/video/delete",Map.of("id",v.getVideoId())).statusCode());assertNull(new VideoService().find(v.getVideoId()));assertFalse(Files.exists(media.resolve(v.getSource())));
    }
    @Test void latestTenAndUserPaginationSix()throws Exception{
        var a=account("alice");var b=account("bob");for(int i=0;i<11;i++)seed(a,"Clip "+i,true);seed(a,"Private clip",false);seed(b,"Bob clip",true);login("alice");
        var home=get("/home");assertEquals(200,home.statusCode());assertEquals(10,cards(home.body()));assertFalse(home.body().contains("Private clip"));assertTrue(home.body().contains("Bob clip"));
        var page=get("/video");assertEquals(6,cards(page.body()));assertFalse(page.body().contains("Bob clip"));assertTrue(page.body().contains("Trang 1 / 2"));var second=get("/video?page=2");assertEquals(6,cards(second.body()));assertTrue(second.body().contains("Trang 2 / 2"));
    }
    @Test void ownershipAndPrivateVideoAreEnforced()throws Exception{
        var a=account("alice");account("bob");var v=seed(a,"Only Alice",false);login("bob");assertEquals(404,get("/video/edit?id="+v.getVideoId()).statusCode());assertEquals(404,get("/video/detail?id="+v.getVideoId()).statusCode());
        assertEquals(404,post("/video/delete",Map.of("id",v.getVideoId())).statusCode());assertEquals(404,multipart("/video/update",Map.of("id",v.getVideoId(),"title","Hacked","active","1"),Map.of()).statusCode());assertEquals("Only Alice",new VideoService().find(v.getVideoId()).getTitle());
        assertEquals(404,get("/media/video?id="+v.getVideoId()).statusCode());
    }
    @Test void nativeMultipartExampleAndBadUpload()throws Exception{
        var u=account("alice");login("alice");var page=get("/upload-demo");assertEquals(200,page.statusCode());assertTrue(page.body().contains("multipart/form-data"));assertTrue(page.body().contains("name=\"multiPartServlet\""));
        assertEquals(400,multipart("/multiPartServlet",Map.of("title","Bad","active","1"),Map.of("multiPartServlet","not video".getBytes())).statusCode());assertEquals(0,new VideoService().byUser(u.getId(),1).total());
        var res=multipart("/multiPartServlet",Map.of("title","Multipart sample","active","1"),Map.of("multiPartServlet",Files.readAllBytes(Path.of("samples/demo-video.mp4"))));assertEquals(302,res.statusCode(),res.body());
    }
    @Test void demoMailboxIsDisabledOutsideExplicitDemoMode()throws Exception{
        System.setProperty("app.demo","false");try{assertEquals(404,get("/demo/mail").statusCode());}finally{System.setProperty("app.demo","true");}
    }
    @Test void csrfAndLogoutMethodAreProtected()throws Exception{
        assertEquals(403,post("/register",Map.of("csrf","bad")).statusCode());assertEquals(405,get("/logout").statusCode());
    }

    @Test void profileUsesSiteMeshBootstrapAndValidatesForms()throws Exception{
        assertEquals(302,get("/profile").statusCode());var u=account("alice");login("alice");var html=get("/profile");assertEquals(200,html.statusCode(),html.body());assertTrue(html.body().contains("data-layout=\"sitemesh-bootstrap\""));assertTrue(html.body().contains("bootstrap.min.css"));assertEquals(1,html.body().split("<header>",-1).length-1);assertFalse(html.body().contains("sitemesh:write"));
        var bad=multipart("/profile/update",Map.of("fullName"," ","phone","abc"),Map.of());assertEquals(400,bad.statusCode());assertEquals("Người dùng alice",new AuthService().find(u.getId()).getFullName());
        assertEquals(302,multipart("/profile/update",Map.of("fullName","Tên mới","phone","0901234567"),Map.of()).statusCode());assertEquals("Tên mới",new AuthService().find(u.getId()).getFullName());
        assertEquals(400,multipart("/profile/update",Map.of("fullName","Tên mới","phone","0901234567"),Map.of("images","not an image".getBytes())).statusCode());
        assertEquals(403,post("/profile/email/request",Map.of("csrf","bad","email","new@example.com")).statusCode());assertEquals(400,post("/profile/email/request",Map.of("email","invalid")).statusCode());
    }
    @Test void profileEmailOtpFlowThroughHttp()throws Exception{
        var u=account("alice");login("alice");assertEquals(302,post("/profile/email/request",Map.of("email","new@example.test")).statusCode());assertEquals("alice@example.test",new AuthService().find(u.getId()).getEmail());String mail;try(var files=Files.list(outbox)){mail=Files.readString(files.findFirst().orElseThrow());}assertTrue(mail.contains("To: new@example.test"));var m=Pattern.compile("Mã OTP của bạn: ([0-9]{6})").matcher(mail);assertTrue(m.find());String code=m.group(1);
        assertEquals(400,post("/profile/email/confirm",Map.of("otp","wrong")).statusCode());assertEquals(302,post("/profile/email/confirm",Map.of("otp",code)).statusCode());assertEquals("new@example.test",new AuthService().find(u.getId()).getEmail());assertEquals(400,post("/profile/email/confirm",Map.of("otp",code)).statusCode());
    }
}
