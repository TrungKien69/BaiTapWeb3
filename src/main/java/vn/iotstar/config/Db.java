package vn.iotstar.config;
import jakarta.persistence.*;
import java.util.function.Function;
public final class Db {
    private Db() {}
    public static <T> T read(Function<EntityManager,T> work) {
        EntityManager em=JpaConfig.getEntityManager(); try{return work.apply(em);}finally{em.close();}
    }
    public static <T> T write(Function<EntityManager,T> work) {
        EntityManager em=JpaConfig.getEntityManager(); var tx=em.getTransaction();
        try{tx.begin();T result=work.apply(em);tx.commit();return result;}
        catch(RuntimeException e){if(tx.isActive())tx.rollback();throw e;}finally{em.close();}
    }
}
