package server.endpoints;

import domain.*;
import io.javalin.Javalin;
import lombok.val;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import java.util.stream.Collectors;

public class CommentEndpoints {
    public static void create(Javalin app, EntityManagerFactory entityManagerFactory) {
        app.post("boards/:idboard/lists/:idlist/cards/:idcard/comments", ctx -> {
            AuthEndpoint.enableAuthCORSFix(ctx);
            DomainUser domainUser = AuthEndpoint.getUserInfo(ctx, entityManagerFactory);
            int boardId = Integer.valueOf(ctx.pathParam("idboard"));
            int listId = Integer.valueOf(ctx.pathParam("idlist"));
            int id = Integer.valueOf(ctx.pathParam("idcard"));
            DomainComment domainComment = ctx.bodyAsClass(DomainComment.class);
            val tmp = entityManagerFactory.createEntityManager();
            val managerTransaction = tmp.getTransaction();
            managerTransaction.begin();

            try {
                val result = tmp.createQuery("SELECT t FROM Board t where t.id=:id", DomainBoard.class).setParameter("id", boardId).getSingleResult();
                DomainList list = result.getLists().stream().filter(item -> item.getId().equals(listId)).collect(Collectors.toList()).get(0);
                DomainCard card = list.getCards().stream().filter(item -> item.getId().equals(id)).collect(Collectors.toList()).get(0);
                card.getComments().add(DomainComment.builder()
                        .value(domainComment.getValue())
                        .addedBy(domainUser.getLogin()).build());
                tmp.persist(result);
                managerTransaction.commit();
            } catch (PersistenceException pe) {
                ctx.result(pe.toString());
            } catch (Exception ex) {
                ctx.result(ex.toString());
            } finally {
                tmp.close();
            }
        });

        app.post("boards/:idboard/lists/:idlist/cards/:idcard/comments/:idcomment/delete", ctx -> {
            AuthEndpoint.enableAuthCORSFix(ctx);
            int boardId = Integer.valueOf(ctx.pathParam("idboard"));
            int listId = Integer.valueOf(ctx.pathParam("idlist"));
            int cardId = Integer.valueOf(ctx.pathParam("idcard"));
            int id = Integer.valueOf(ctx.pathParam("idcomment"));

            val tmp = entityManagerFactory.createEntityManager();
            val managerTransaction = tmp.getTransaction();
            managerTransaction.begin();

            try {
                val result = tmp.createQuery("SELECT t FROM Board t where t.id=:id", DomainBoard.class).setParameter("id", boardId).getSingleResult();
                DomainList list = result.getLists().stream().filter(item -> item.getId().equals(listId)).collect(Collectors.toList()).get(0);
                DomainCard card = list.getCards().stream().filter(item -> item.getId().equals(cardId)).collect(Collectors.toList()).get(0);
                DomainComment comment = card.getComments().stream().filter(item -> item.getId().equals(id)).collect(Collectors.toList()).get(0);
                card.getComments().remove(comment);
                tmp.persist(result);
                managerTransaction.commit();
            } catch (PersistenceException pe) {
                ctx.result(pe.toString());
            } catch (Exception ex) {
                ctx.result(ex.toString());
            } finally {
                tmp.close();
            }
        });
    }
}
