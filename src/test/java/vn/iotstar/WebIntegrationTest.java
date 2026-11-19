package vn.iotstar;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;
import java.io.*;
import org.apache.catalina.startup.Tomcat;
import vn.iotstar.dev.DevServer;
import vn.iotstar.config.JpaConfig;
import vn.iotstar.dao.impl.CategoryDao;
import vn.iotstar.entity.Category;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WebIntegrationTest {
    Tomcat server; HttpClient client; String base; Path images; String csrf;
    final CategoryDao dao=new CategoryDao();
    @BeforeAll void start() throws Exception {
        images=Files.createTempDirectory("jpa-images-"); System.setProperty("upload.dir", images.toString());
        server=DevServer.start(0, Files.createTempDirectory("jpa-tomcat-"));
        base="http://localhost:"+server.getConnector().getLocalPort()+"/jpa-category";
        CookieManager cookies=new CookieManager(); cookies.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        client=HttpClient.newBuilder().cookieHandler(cookies).build();
        vn.iotstar.config.Db.write(em -> {
            var u = new vn.iotstar.entity.AppUser();u.setUsername("categorytester");u.setEmail("category@test.example");u.setFullName("Category tester");u.setActive(true);u.setPasswordHash(vn.iotstar.security.PasswordHasher.hash("CategoryTest9!"));em.persist(u);return null;
        });
        String loginHtml=get("/login").body();
        Matcher loginToken=Pattern.compile("name=\"csrf\" value=\"([^\"]+)\"").matcher(loginHtml);
        assertTrue(loginToken.find());
        assertEquals(302,post("/login",Map.of("csrf",loginToken.group(1),"identity","categorytester","password","CategoryTest9!")).statusCode());
        String html=get("/admin/category/add").body();
        Matcher matcher=Pattern.compile("name=\"csrf\" value=\"([^\"]+)\"").matcher(html);
        assertTrue(matcher.find(), html); csrf=matcher.group(1);
    }
    @AfterAll void stop() throws Exception { if(server!=null){server.stop();server.destroy();} System.clearProperty("upload.dir"); JpaConfig.close(); }
    @BeforeEach void clean() throws Exception {
        var em=JpaConfig.getEntityManager();
        try {em.getTransaction().begin();em.createQuery("DELETE FROM Video").executeUpdate();em.createQuery("DELETE FROM Category").executeUpdate();em.getTransaction().commit();}finally{em.close();}
        try(var paths=Files.list(images)){for(Path p:paths.toList())Files.delete(p);}
    }
    HttpResponse<String> get(String path) throws Exception {return client.send(HttpRequest.newBuilder(URI.create(base+path)).GET().build(),HttpResponse.BodyHandlers.ofString());}
    HttpResponse<String> post(String path, Map<String,String> fields) throws Exception {
        String body=fields.entrySet().stream().map(e->URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)+"="+URLEncoder.encode(e.getValue(),StandardCharsets.UTF_8)).collect(java.util.stream.Collectors.joining("&"));
        return client.send(HttpRequest.newBuilder(URI.create(base+path)).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
    }
    HttpResponse<String> multipart(String path, Map<String,String> fields, byte[] file) throws Exception {
        String boundary="----JpaBoundary"+UUID.randomUUID();
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        for(var e:fields.entrySet())out.write(("--"+boundary+"\r\nContent-Disposition: form-data; name=\""+e.getKey()+"\"\r\n\r\n"+e.getValue()+"\r\n").getBytes(StandardCharsets.UTF_8));
        if(file!=null){out.write(("--"+boundary+"\r\nContent-Disposition: form-data; name=\"images1\"; filename=\"test.png\"\r\nContent-Type: image/png\r\n\r\n").getBytes(StandardCharsets.UTF_8));out.write(file);out.write("\r\n".getBytes());}
        out.write(("--"+boundary+"--\r\n").getBytes());
        return client.send(HttpRequest.newBuilder(URI.create(base+path)).header("Content-Type","multipart/form-data; boundary="+boundary).POST(HttpRequest.BodyPublishers.ofByteArray(out.toByteArray())).build(),HttpResponse.BodyHandlers.ofString());
    }
    Map<String,String> fields(String name){Map<String,String> f=new HashMap<>();f.put("csrf",csrf);f.put("categoryname",name);f.put("status","1");return f;}
    byte[] png(){return Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jRZkAAAAASUVORK5CYII=");}
    @Test void jspPagesAndUtf8Crud() throws Exception {
        var response=multipart("/admin/category/insert",fields("Điện thoại"),null); assertEquals(302,response.statusCode(),response.body());
        Category c=dao.findAll().get(0); assertEquals("Điện thoại",c.getCategoryname());
        var page=get("/admin/categories");assertEquals(200,page.statusCode());assertTrue(page.body().contains("Điện thoại"));
        var edit=get("/admin/category/edit?id="+c.getCategoryid());assertEquals(200,edit.statusCode(),edit.body());
        var f=fields("Phụ kiện");f.put("categoryid",""+c.getCategoryid());f.put("status","0");
        assertEquals(302,multipart("/admin/category/update",f,null).statusCode());assertEquals(0,dao.findById(c.getCategoryid()).getStatus());
        assertEquals(302,post("/admin/category/delete",Map.of("csrf",csrf,"categoryid",""+c.getCategoryid())).statusCode());assertEquals(0,dao.count());
    }
    @Test void uploadServeRetainReplaceAndDeleteImage() throws Exception {
        var result=multipart("/admin/category/insert",fields("Ảnh"),png());assertEquals(302,result.statusCode(),result.body());
        Category c=dao.findAll().get(0);String image=c.getImages();assertTrue(Files.exists(images.resolve(image)));
        HttpResponse<byte[]> served=client.send(HttpRequest.newBuilder(URI.create(base+"/image?fname="+image)).GET().build(),HttpResponse.BodyHandlers.ofByteArray());
        assertEquals(200,served.statusCode());assertArrayEquals(png(),served.body());
        var f=fields("Ảnh mới");f.put("categoryid",""+c.getCategoryid());
        assertEquals(302,multipart("/admin/category/update",f,null).statusCode());assertEquals(image,dao.findById(c.getCategoryid()).getImages());
        f.put("images","https://example.com/image.png");assertEquals(302,multipart("/admin/category/update",f,null).statusCode());assertFalse(Files.exists(images.resolve(image)));
        assertEquals(302,multipart("/admin/category/update",f,png()).statusCode());String lastImage=dao.findById(c.getCategoryid()).getImages();
        assertEquals(302,post("/admin/category/delete",Map.of("csrf",csrf,"categoryid",""+c.getCategoryid())).statusCode());assertFalse(Files.exists(images.resolve(lastImage)));
    }
    @Test void invalidAndDuplicateRequestsDoNotLeaveFiles() throws Exception {
        assertEquals(302,multipart("/admin/category/insert",fields("Laptop"),null).statusCode());
        assertEquals(400,multipart("/admin/category/insert",fields("Laptop"),png()).statusCode());
        try(var paths=Files.list(images)){assertEquals(0,paths.count());}
        assertEquals(400,multipart("/admin/category/insert",fields("Fake"),"not an image".getBytes()).statusCode());assertEquals(1,dao.count());
        var f=fields("Bad URL");f.put("images","javascript:alert(1)");assertEquals(400,multipart("/admin/category/insert",f,null).statusCode());
        assertEquals(400,multipart("/admin/category/insert",fields("Large"),new byte[2097153]).statusCode());assertEquals(1,dao.count());
    }
    @Test void csrfAndIdsAndGetDelete() throws Exception {
        assertEquals(403,post("/admin/category/delete",Map.of("categoryid","1")).statusCode());
        assertEquals(400,get("/admin/category/edit?id=abc").statusCode());assertEquals(404,get("/admin/category/edit?id=999999").statusCode());
        assertEquals(405,get("/admin/category/delete?id=1").statusCode());assertEquals(404,get("/image?fname=../pom.xml").statusCode());
    }
    @Test void listEscapesHtmlAndSupportsSearchPagination() throws Exception {
        for(int i=0;i<7;i++)assertEquals(302,multipart("/admin/category/insert",fields("Laptop "+i),null).statusCode());
        assertEquals(302,multipart("/admin/category/insert",fields("<script>alert(1)</script>"),null).statusCode());
        var html=get("/admin/categories").body();assertTrue(html.contains("&lt;script&gt;"));assertFalse(html.contains("<script>alert(1)</script>"));
        assertTrue(get("/admin/categories?q=Laptop&page=2").body().contains("Trang 2 / 2"));
        assertTrue(get("/admin/categories?q=KhongCo").body().contains("Chưa có danh mục phù hợp"));
    }
}
