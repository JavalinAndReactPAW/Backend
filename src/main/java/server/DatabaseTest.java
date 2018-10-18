package server;

import domain.DomainBoard;
import domain.DomainList;
import io.javalin.Javalin;
import lombok.val;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.ArrayList;
import java.util.List;

public class DatabaseTest {

    //Mock do sprawdzenia czy baza dziala
    public static void doMagic() {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("NewPersistenceUnit");
        EntityManager manager = factory.createEntityManager();
        manager.getTransaction().begin();

        List<DomainList> listCollection = new ArrayList<>();
        listCollection.add(DomainList.builder().card("TEST").build());
        listCollection.add(DomainList.builder().card("QWERTY").build());
        listCollection.add(DomainList.builder().card("JAVALIN").build());
        listCollection.add(DomainList.builder().card("JESTSUPER").build());
        DomainBoard table = DomainBoard.builder().name("TESTOWA TABLICA").lists(listCollection).build();

        manager.persist(table);

        listCollection = new ArrayList<>();
        listCollection.add(DomainList.builder().card("LISTY").build());
        listCollection.add(DomainList.builder().card("DUZO").build());
        listCollection.add(DomainList.builder().card("FAJNE").build());
        listCollection.add(DomainList.builder().card("OK").build());
        table = DomainBoard.builder().name("TABLICA NR 2").lists(listCollection).build();

        manager.persist(table);

        manager.getTransaction().commit();
        manager.close();

        Javalin app = Javalin.create();
        app.enableCorsForAllOrigins();
        app.start(7000);

        app.get("/", ctx -> {
            val tmp = factory.createEntityManager();
            tmp.getTransaction().begin();
            val result = tmp.createQuery("SELECT t FROM Board t", DomainBoard.class).getResultList();
            tmp.getTransaction().commit();
            tmp.close();
            ctx.json(result);
        });

        app.get("/remove/:id", ctx -> {
            val tmp = factory.createEntityManager();
            try {
                tmp.getTransaction().begin();
                val item = tmp.find(DomainBoard.class, Integer.valueOf(ctx.pathParam("id")));
                tmp.remove(item);
                tmp.getTransaction().commit();
                tmp.close();
                ctx.result("OK");
            } catch (Exception ex) {
                ctx.result(ex.toString());
            } finally {
                tmp.close();
            }
        });
    }
}
