package server.endpoints;

import domain.DomainCard;
import domain.DomainList;
import io.javalin.Javalin;
import lombok.val;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;

public class CardEndpoints {
    public static void create(Javalin app, EntityManagerFactory entityManagerFactory) {
        app.post("card/new", ctx -> {
            val listId = ctx.header("listId");
            DomainCard domainCard = ctx.bodyAsClass(DomainCard.class);
            val cardId = domainCard.getId();
            val cardName = domainCard.getName();
            val cardValue = domainCard.getValue();
            val entityManager = entityManagerFactory.createEntityManager();
            val managerTransaction = entityManager.getTransaction();
            try {
                managerTransaction.begin();
                String query = "insert into CARD values(?, ?, ?)";

                entityManager.createNativeQuery(query)
                        .setParameter(1, cardId)
                        .setParameter(2, cardName)
                        .setParameter(3, cardValue)
                        .executeUpdate();
                ctx.result("Added new board successfully. ");
                String query2 = "insert into LIST_CARD  values(?, ?)";

                entityManager.createNativeQuery(query2)
                        .setParameter(1, listId)
                        .setParameter(2, cardId)
                        .executeUpdate();

                managerTransaction.commit();
            } catch (PersistenceException pe) {
                ctx.result("Could not add new card: ID is probably not unique. Full exception --> " + pe.toString());
            } catch (Exception ex) {
                ctx.result(ex.toString());
            } finally {
                entityManager.close();
            }
        });

        app.get("/card/:id", ctx -> {
            int id = Integer.valueOf(ctx.pathParam("id"));
            val tmp = entityManagerFactory.createEntityManager();
            try {
                val result = tmp.createQuery("SELECT t FROM Card t where t.id=:id", DomainCard.class).setParameter("id", id).getSingleResult();
                ctx.json(result);
            } catch (Exception ex) {
                ctx.result(ex.toString());
            } finally {
                tmp.close();
            }
        });
    }
}
