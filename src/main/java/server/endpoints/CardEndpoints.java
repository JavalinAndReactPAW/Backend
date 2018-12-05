package server.endpoints;

import domain.DomainBoard;
import domain.DomainCard;
import domain.DomainList;
import domain.DomainUser;
import io.javalin.Javalin;
import lombok.val;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import java.util.stream.Collectors;

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

        app.patch("boards/:board/lists/:from/cards/:id/move/:to", ctx -> {
            AuthEndpoint.enableAuthCORSFix(ctx);
            int boardId = Integer.valueOf(ctx.pathParam("board"));
            int id = Integer.valueOf(ctx.pathParam("id"));
            int fromId = Integer.valueOf(ctx.pathParam("from"));
            int toId = Integer.valueOf(ctx.pathParam("to"));
            val tmp = entityManagerFactory.createEntityManager();
            val managerTransaction = tmp.getTransaction();
            managerTransaction.begin();
            try {
                val result = tmp.createQuery("SELECT t FROM Board t where t.id=:id", DomainBoard.class).setParameter("id", boardId).getSingleResult();
                DomainList fromList =  result.getLists().stream().filter(item -> item.getId().equals(fromId)).collect(Collectors.toList()).get(0);
                DomainList toList =  result.getLists().stream().filter(item -> item.getId().equals(toId)).collect(Collectors.toList()).get(0);
                DomainCard domainCard = fromList.getCards().stream().filter(item -> item.getId().equals(id)).collect(Collectors.toList()).get(0);

                fromList.getCards().remove(domainCard);
                tmp.persist(result);
                managerTransaction.commit();

                toList.getCards().add(domainCard);
                tmp.persist(result);
                managerTransaction.begin();
                managerTransaction.commit();

                ctx.result(result.toString());

            } catch (IndexOutOfBoundsException ex) {
                ctx.result(ex.toString());
            } finally {
                tmp.close();
            }
        });
    }
}
