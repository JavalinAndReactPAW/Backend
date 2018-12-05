package server.endpoints;

import domain.DomainBoard;
import domain.DomainList;
import domain.DomainUser;
import io.javalin.Javalin;
import lombok.val;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;

public class ListEndpoints {

    public static void create(Javalin app, EntityManagerFactory entityManagerFactory) {

        //Old, for reference.
        /*app.post("/boards/:id/lists", ctx -> {
            val boardId = Integer.valueOf(ctx.pathParam("id"));
            System.out.println(boardId);
            DomainList domainList = ctx.bodyAsClass(DomainList.class);
            int listId = domainList.getId();
            String listName = domainList.getName();
            val entityManager = entityManagerFactory.createEntityManager();
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
        });*/


        app.post("/boards/:id/lists", ctx -> {
            AuthEndpoint.enableAuthCORSFix(ctx);
            DomainUser domainUser = AuthEndpoint.getUserInfo(ctx, entityManagerFactory);
            val userBoards = domainUser.getBoardIds();
            DomainList inputList = ctx.bodyAsClass(DomainList.class);
            val boardId = Integer.valueOf(ctx.pathParam("id"));
            val entityManager = entityManagerFactory.createEntityManager();
            val managerTransaction = entityManager.getTransaction();
            try {
                managerTransaction.begin();
                val modifiedBoard = entityManager.createQuery("SELECT t FROM Board t where t.id=:id", DomainBoard.class).setParameter("id", boardId).getSingleResult();
                if (!userBoards.contains(modifiedBoard.getId()))
                    ctx.status(403);
                else {
                    modifiedBoard.getLists().add(inputList);
                    entityManager.persist(modifiedBoard);
                    managerTransaction.commit();
                }
            } catch (PersistenceException pe) {
                ctx.result("Could not add new board: ID is probably not unique. Full exception--> " + pe.toString());
            } catch (Exception ex) {
                ctx.result(ex.toString());
            } finally {
                entityManager.close();
            }
        });
    }
}
