package server.endpoints;

import domain.DomainList;
import io.javalin.Javalin;
import lombok.val;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;

public class ListEndpoints {

    public static void create(Javalin app, EntityManagerFactory entityManagerFactory) {
        app.post("list/new", ctx -> {
            val boardId = ctx.header("boardId");
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
        });
    }
}
