package server.endpoints;

import domain.DomainBoard;
import domain.DomainCard;
import domain.DomainList;
import domain.DomainUser;
import io.javalin.Javalin;
import lombok.val;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

public class RemainingEndpoints {
    public static void create(Javalin app, EntityManagerFactory factory) {

        app.get("/", ctx -> {
            val tmp = factory.createEntityManager();
            val result = tmp.createQuery("SELECT t FROM Board t", DomainBoard.class).getResultList();
            result.forEach(domainBoard -> domainBoard.setLists(null));
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
