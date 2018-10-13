package server;

import java.sql.SQLException;

public class StartApp {
    public static void main(String[] args) throws SQLException {
        org.h2.tools.Server.createWebServer().start();
        DatabaseTest.doMagic();
    }
}