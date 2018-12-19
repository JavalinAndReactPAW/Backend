package server.endpoints;

import domain.BoardState;
import domain.DomainBoard;
import domain.DomainUser;
import io.javalin.Javalin;
import lombok.val;
import lombok.var;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import java.util.stream.Collectors;

public class BoardEndpoints {
    public static void create(Javalin app, EntityManagerFactory factory) {

        app.get("/boards", ctx -> {
            AuthEndpoint.enableAuthCORSFix(ctx);
            val tmp = factory.createEntityManager();
            DomainUser domainUser = AuthEndpoint.getUserInfo(ctx, factory);
            try {
                var result = tmp.createQuery("SELECT t FROM Board t WHERE id IN :ids", DomainBoard.class)
                        .setParameter("ids", domainUser.getBoardIds()).getResultList();
                result.forEach(domainBoard -> domainBoard.setLists(null));
                result = result.stream().filter(domainBoard -> !domainBoard.getBoardState().equals(BoardState.DELETED))
                        .collect(Collectors.toList());
                ctx.json(result);
            } catch (Exception ex) {
                ctx.status(401);
            } finally {
                tmp.close();
            }
        });

        app.get("/boards/:id", ctx -> {
            AuthEndpoint.enableAuthCORSFix(ctx);
            int id = Integer.valueOf(ctx.pathParam("id"));
            val tmp = factory.createEntityManager();
            DomainUser domainUser = AuthEndpoint.getUserInfo(ctx, factory);
            try {
                if (!domainUser.getBoardIds().contains(id))
                    ctx.status(403);
                else {
                    val result = tmp.createQuery("SELECT t FROM Board t where t.id=:id", DomainBoard.class).setParameter("id", id).getSingleResult();
                    ctx.json(result);
                }
            } catch (Exception ex) {
                ctx.result(ex.toString());
            } finally {
                tmp.close();
            }
        });

        app.patch("/boards/:id/:action", ctx -> {
            AuthEndpoint.enableAuthCORSFix(ctx);
            int id = Integer.valueOf(ctx.pathParam("id"));
            String action = String.valueOf(ctx.pathParam("action"));
            val tmp = factory.createEntityManager();
            val transaction = tmp.getTransaction();
            DomainUser domainUser = AuthEndpoint.getUserInfo(ctx, factory);
            transaction.begin();
            try {
                if (!domainUser.getBoardIds().contains(id))
                    ctx.status(403);
                else {
                    val result = tmp.createQuery("SELECT t FROM Board t where t.id=:id", DomainBoard.class).setParameter("id", id).getSingleResult();
                    if (action.equals("disable")) {
                        result.setBoardState(BoardState.DISABLED);
                    } else if (action.equals("delete")) {
                        result.setBoardState(BoardState.DELETED);
                    } else if (action.equals("enable") && result.getBoardState().equals(BoardState.DISABLED)) {
                        result.setBoardState(BoardState.ACTIVE);
                    }
                    tmp.persist(result);
                    transaction.commit();
                }
            } catch (Exception ex) {
                ctx.result(ex.toString());
            } finally {
                tmp.close();
            }
        });

        app.post("boards/new", ctx -> {
            AuthEndpoint.enableAuthCORSFix(ctx);
            DomainUser domainUser = AuthEndpoint.getUserInfo(ctx, factory);
            DomainBoard inputBoard = ctx.bodyAsClass(DomainBoard.class);
            val entityManager = factory.createEntityManager();
            val managerTransaction = entityManager.getTransaction();
            try {
                managerTransaction.begin();
                val createdBoard = DomainBoard.builder().name(inputBoard.getName()).boardState(BoardState.ACTIVE).build();
                entityManager.persist(createdBoard);
                entityManager.flush();
                String query2 = "insert into USER_BOARDIDS values(?, ?)";
                entityManager.createNativeQuery(query2)
                        .setParameter(1, domainUser.getId())
                        .setParameter(2, createdBoard.getId())
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
