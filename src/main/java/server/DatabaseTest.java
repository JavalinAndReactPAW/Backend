package server;

import domain.DomainList;
import domain.DomainTable;
import io.javalin.Javalin;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.ArrayList;
import java.util.List;


public class DatabaseTest {

    //Mock do sprawdzenia czy baza dziala
    public static void doMagic(){
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("NewPersistenceUnit");
        EntityManager manager = factory.createEntityManager();
        manager.getTransaction().begin();

        List<DomainList> listCollection = new ArrayList<>();
        listCollection.add(DomainList.builder().card("TEST").build());
        listCollection.add(DomainList.builder().card("QWERTY").build());
        listCollection.add(DomainList.builder().card("JAVALIN").build());
        listCollection.add(DomainList.builder().card("JESTSUPER").build());
        DomainTable table = DomainTable.builder().name("TESTOWA TABELA").lists(listCollection).build();
        listCollection.forEach(x -> manager.persist(x));
        manager.persist(table);

        List<DomainTable> domainTables = manager.createQuery("SELECT t FROM Table t",DomainTable.class).getResultList();

        manager.getTransaction().commit();
        manager.close();

        Javalin app = Javalin.create().start(7000);
        app.get("/", ctx -> ctx.json(domainTables));
    }
}
