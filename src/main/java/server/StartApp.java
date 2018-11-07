package server;

import io.javalin.Javalin;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.sql.SQLException;

public class StartApp {
    public static void main(String[] args) throws SQLException {
        org.h2.tools.Server.createWebServer().start();
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("NewPersistenceUnit");
        DatabaseMock.createMock(factory);

        Javalin app = Javalin.create();
        app.enableCorsForAllOrigins();

        SampleEndpoints.createEndpoints(app,factory);
        AuthEndpoint.addAuthEndpoint(app,factory);
        BoardEndpoint.createBoardEndpoints(app,factory);

        app.start(7000);

    }
}