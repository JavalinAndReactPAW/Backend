package server;

import io.javalin.Javalin;
import server.endpoints.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.sql.SQLException;

public class StartApp {
    public static void main(String[] args) throws SQLException {
        org.h2.tools.Server.createWebServer().start();
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("NewPersistenceUnit");
        DatabaseMock.createMock(entityManagerFactory);

        Javalin app = Javalin.create();
        app.enableCorsForAllOrigins();

        BoardEndpoints.create(app, entityManagerFactory);
        ListEndpoints.create(app, entityManagerFactory);
        CardEndpoints.create(app, entityManagerFactory);
        RemainingEndpoints.create(app, entityManagerFactory);
        AuthEndpoint.addAuthEndpoint(app, entityManagerFactory);

        app.start(7000);
    }
}