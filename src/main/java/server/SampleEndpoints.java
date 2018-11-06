package server;

import domain.DomainBoard;
import domain.DomainCard;
import io.javalin.Javalin;
import lombok.val;

import javax.persistence.EntityManagerFactory;

public class SampleEndpoints {


    public static void createEndpoints(Javalin app, EntityManagerFactory factory) {

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
