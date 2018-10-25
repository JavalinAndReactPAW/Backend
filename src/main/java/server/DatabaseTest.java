package server;

import com.fasterxml.jackson.annotation.JsonView;
import domain.DomainBoard;
import domain.DomainCard;
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
        List<DomainCard> cardCollection = new ArrayList<>();
        cardCollection.add(DomainCard.builder().name("TEST1").value("kartaJedna").build());
        cardCollection.add(DomainCard.builder().name("TEST2").value("kartaDruga").build());
        listCollection.add(DomainList.builder().name("Lista1").cards(cardCollection).build());
        cardCollection = new ArrayList<>();
        cardCollection.add(DomainCard.builder().name("TEST1").value("kartaJedna").build());
        cardCollection.add(DomainCard.builder().name("TEST2").value("kartaDruga").build());
        listCollection.add(DomainList.builder().name("Lista2").cards(cardCollection).build());
        cardCollection = new ArrayList<>();
        cardCollection.add(DomainCard.builder().name("TEST1").value("kartaJedna").build());
        cardCollection.add(DomainCard.builder().name("TEST2").value("kartaDruga").build());
        listCollection.add(DomainList.builder().name("Lista3").cards(cardCollection).build());
        cardCollection = new ArrayList<>();
        cardCollection.add(DomainCard.builder().name("TEST1").value("kartaJedna").build());
        cardCollection.add(DomainCard.builder().name("TEST2").value("kartaDruga").build());
        listCollection.add(DomainList.builder().name("Lista4").cards(cardCollection).build());
        DomainBoard table = DomainBoard.builder().name("TESTOWA TABLICA").lists(listCollection).build();

        manager.persist(table);

        listCollection = new ArrayList<>();
        cardCollection = new ArrayList<>();
        cardCollection.add(DomainCard.builder().name("TEST1").value("kartaJedna").build());
        cardCollection.add(DomainCard.builder().name("TEST2").value("kartaDruga").build());
        listCollection.add(DomainList.builder().name("Lista5").cards(cardCollection).build());
        cardCollection = new ArrayList<>();
        cardCollection.add(DomainCard.builder().name("TEST1").value("kartaJedna").build());
        cardCollection.add(DomainCard.builder().name("TEST2").value("kartaDruga").build());
        listCollection.add(DomainList.builder().name("Lista6").cards(cardCollection).build());
        cardCollection = new ArrayList<>();
        cardCollection.add(DomainCard.builder().name("TEST1").value("kartaJedna").build());
        cardCollection.add(DomainCard.builder().name("TEST2").value("kartaDruga").build());
        listCollection.add(DomainList.builder().name("Lista7").cards(cardCollection).build());
        cardCollection = new ArrayList<>();
        cardCollection.add(DomainCard.builder().name("TEST1").value("kartaJedna").build());
        cardCollection.add(DomainCard.builder().name("TEST2").value("kartaDruga").build());
        listCollection.add(DomainList.builder().name("Lista8").cards(cardCollection).build());
        DomainBoard table2 = DomainBoard.builder().name("TESTOWA TABLICA 2").lists(listCollection).build();

        manager.persist(table2);

        manager.getTransaction().commit();
        manager.close();

        Javalin app = Javalin.create();
        app.enableCorsForAllOrigins();
        app.start(7000);

        app.get("/", ctx -> {
            val tmp = factory.createEntityManager();
            val result = tmp.createQuery("SELECT t FROM Board t", DomainBoard.class).getResultList();
            result.forEach(domainBoard -> domainBoard.setLists(null));
            tmp.close();
            ctx.json(result);
        });

        app.get("/board/:id", ctx -> {
            int id = Integer.valueOf(ctx.pathParam("id"));
            val tmp = factory.createEntityManager();
            try {
                val result = tmp.createQuery("SELECT t FROM Board t where t.id=:id", DomainBoard.class).setParameter("id", id).getSingleResult();
                ctx.json(result);
            } catch (Exception ex) {
                ctx.result(ex.toString());
            } finally {
                tmp.close();
            }
        });

        app.get("/card/:id", ctx -> {
            int id = Integer.valueOf(ctx.pathParam("id"));
            val tmp = factory.createEntityManager();
            try {
                val result = tmp.createQuery("SELECT t FROM Card t where t.id=:id", DomainCard.class).setParameter("id", id).getSingleResult();
                ctx.json(result);
            } catch (Exception ex) {
                ctx.result(ex.toString());
            } finally {
                tmp.close();
            }
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
