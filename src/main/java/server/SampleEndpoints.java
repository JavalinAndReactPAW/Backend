package server;

import domain.DomainBoard;
import domain.DomainCard;
import domain.DomainList;
import io.javalin.Javalin;
import lombok.val;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

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

        app.post("board/new", ctx -> {
            val userId = ctx.header("user");
            DomainBoard domainBoard = ctx.bodyAsClass(DomainBoard.class);
            int boardId = domainBoard.getId();
            String name = domainBoard.getName();
            val entityManager = factory.createEntityManager();
            val managerTransaction = entityManager.getTransaction();
            try {
                managerTransaction.begin();
                String query = "insert into BOARD values(?, ?)";

                entityManager.createNativeQuery(query)
                        .setParameter(1, boardId)
                        .setParameter(2, name)
                        .executeUpdate();
                ctx.result("Added new board successfully. ");
                String query2 = "insert into USER_BOARDIDS values(?, ?)";

                entityManager.createNativeQuery(query2)
                        .setParameter(1, userId)
                        .setParameter(2, boardId)
                        .executeUpdate();

                managerTransaction.commit();
            } catch (PersistenceException pe) {
                ctx.result("Could not add new board: ID is probably not unique. Full exception--> " + pe.toString());
            } catch (Exception ex) {
                ctx.result(ex.toString());
            } finally {
                entityManager.close();
            }
        });

        app.post("list/new", ctx -> {
            val boardId = ctx.header("boardId");
            DomainList domainList = ctx.bodyAsClass(DomainList.class);
            int listId = domainList.getId();
            String listName = domainList.getName();
            val entityManager = factory.createEntityManager();
            val managerTransaction = entityManager.getTransaction();
            try {
                managerTransaction.begin();
                String query = "insert into LIST values(?, ?)";

                entityManager.createNativeQuery(query)
                        .setParameter(1, listId)
                        .setParameter(2, listName)
                        .executeUpdate();
                ctx.result("Added new board successfully. ");
                String query2 = "insert into BOARD_LIST values(?, ?)";

                entityManager.createNativeQuery(query2)
                        .setParameter(1, boardId)
                        .setParameter(2, listId)
                        .executeUpdate();

                managerTransaction.commit();
            } catch (PersistenceException pe) {
                ctx.result("Could not add new list: ID is probably not unique. Full exception --> " + pe.toString());
            } catch (Exception ex) {
                ctx.result(ex.toString());
            } finally {
                entityManager.close();
            }
        });

        app.post("card/new", ctx -> {
            val listId = ctx.header("listId");
            DomainCard domainCard = ctx.bodyAsClass(DomainCard.class);
            val cardId = domainCard.getId();
            val cardName = domainCard.getName();
            val cardValue = domainCard.getValue();
            val entityManager = factory.createEntityManager();
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
