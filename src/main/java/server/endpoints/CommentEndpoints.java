package server.endpoints;

import domain.DomainBoard;
import domain.DomainCard;
import domain.DomainComment;
import domain.DomainList;
import io.javalin.Javalin;
import lombok.val;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import java.util.stream.Collectors;

public class CommentEndpoints {
    public static void create(Javalin app, EntityManagerFactory entityManagerFactory) {
        app.post("/boards/:boardId/lists/:listId/cards/:cardId/comments/", ctx -> {
            int cardId = Integer.valueOf(ctx.pathParam("cardId"));
            int listId = Integer.valueOf(ctx.pathParam("listId"));
            int boardId = Integer.valueOf(ctx.pathParam("boardId"));
            DomainComment inputcomment = ctx.bodyAsClass(DomainComment.class);
            val tmp = entityManagerFactory.createEntityManager();
            val managerTransaction = tmp.getTransaction();
            managerTransaction.begin();
            try {
                val result = tmp.createQuery("SELECT t FROM Board t where t.id=:id", DomainBoard.class).setParameter("id", boardId).getSingleResult();
                DomainList list = result.getLists().stream().filter(item -> item.getId().equals(listId)).collect(Collectors.toList()).get(0);
                DomainCard card = list.getCards().stream().filter(item -> item.getId().equals(cardId)).collect(Collectors.toList()).get(0);

                val createdComment = DomainComment.builder().name(inputcomment.getName()).value(inputcomment.getValue()).build();
                tmp.persist(createdComment);
                tmp.flush();
                managerTransaction.commit();
            } catch (PersistenceException pe) {
                ctx.result("Could not add new card: ID is probably not unique. Full exception --> " + pe.toString());
            } catch (Exception ex) {
                ctx.result(ex.toString());
            } finally {
                tmp.close();
            }
            });
    }

}
