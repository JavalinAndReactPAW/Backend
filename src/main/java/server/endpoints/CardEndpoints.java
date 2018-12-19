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
        app.post("boards/:idboard/lists/:idlist/cards", ctx -> {
            AuthEndpoint.enableAuthCORSFix(ctx);
            int boardId = Integer.valueOf(ctx.pathParam("idboard"));
            int id = Integer.valueOf(ctx.pathParam("idlist"));
            DomainCard domainCard = ctx.bodyAsClass(DomainCard.class);
            val tmp = entityManagerFactory.createEntityManager();
            val managerTransaction = tmp.getTransaction();
            managerTransaction.begin();

            try {
                val result = tmp.createQuery("SELECT t FROM Board t where t.id=:id", DomainBoard.class).setParameter("id", boardId).getSingleResult();
                DomainList list =  result.getLists().stream().filter(item -> item.getId().equals(id)).collect(Collectors.toList()).get(0);
                list.getCards().add(domainCard);
                tmp.persist(result);
                managerTransaction.commit();
            } catch (PersistenceException pe) {
                ctx.result("Could not add new card: ID is probably not unique. Full exception --> " + pe.toString());
            } catch (Exception ex) {
                ctx.result(ex.toString());
            } finally {
                tmp.close();
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
