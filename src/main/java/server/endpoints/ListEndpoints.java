package server.endpoints;

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


        //TODO: Fix binding of added list with table
        app.post("/boards/:id/lists", ctx -> {
            AuthEndpoint.enableAuthCORSFix(ctx);
            DomainUser domainUser = AuthEndpoint.getUserInfo(ctx, entityManagerFactory);
            DomainList inputList = ctx.bodyAsClass(DomainList.class);
            val boardId = Integer.valueOf(ctx.pathParam("id"));
            val entityManager = entityManagerFactory.createEntityManager();
            val managerTransaction = entityManager.getTransaction();
            try {
                managerTransaction.begin();
                val createdList = DomainList.builder().name(inputList.getName()).build();
                entityManager.persist(createdList);
                entityManager.flush();
                val listId = createdList.getId();
                String query = "insert into LIST values(?, ?)";
                entityManager.createNativeQuery(query)
                        .setParameter(1, listId)
                        .setParameter(2, inputList.getName())
                        .executeUpdate();
                managerTransaction.commit();

                String query2 = "insert into BOARD_LIST values(?, ?)";
                entityManager.createNativeQuery(query2)
                        .setParameter(1, boardId)
                        .setParameter(2, listId)
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
    }
}
