package server;

import domain.DomainBoard;
import domain.DomainUser;
import io.javalin.Javalin;
import lombok.val;

import javax.persistence.EntityManagerFactory;

public class BoardEndpoint {

    public static void createBoardEndpoints(Javalin app, EntityManagerFactory factory) {
        app.get("/boards", ctx -> {
            AuthEndpoint.enableAuthCORSFix(ctx);
            val tmp = factory.createEntityManager();
            DomainUser domainUser = AuthEndpoint.getUserInfo(ctx, factory);
            try {
                val result = tmp.createQuery("SELECT t FROM Board t WHERE id IN :ids", DomainBoard.class)
                        .setParameter("ids", domainUser.getBoardIds()).getResultList();
                result.forEach(domainBoard -> domainBoard.setLists(null));
                ctx.json(result);
            } catch (Exception ex) {
                ctx.status(401);
            } finally {
                tmp.close();
            }
        });
    }
}
