package server;

import domain.DomainUser;
import io.javalin.Context;
import io.javalin.Javalin;
import lombok.var;
import org.mindrot.jbcrypt.BCrypt;
import request.LoginRequest;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.persistence.EntityManagerFactory;
import javax.xml.bind.DatatypeConverter;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class AuthEndpoint {

    private static HashMap<String, Integer> tokenUserIdSessionKeeper = new HashMap();

    public static void addAuthEndpoint(Javalin app, EntityManagerFactory factory) {
        app.post("/login", ctx -> {
            enableAuthCORSFix(ctx);
            var loginRequest = ctx.bodyAsClass(LoginRequest.class);
            var entityManager = factory.createEntityManager();
            DomainUser selectedUser = null;
            try {
                selectedUser = entityManager.createQuery("SELECT t FROM User t where t.login=:login", DomainUser.class)
                        .setParameter("login", loginRequest.getLogin()).getSingleResult();
            } catch (Exception ex) {
                ctx.result("Failed to login");
                ctx.status(401);
            } finally {
                entityManager.close();
            }
            if (BCrypt.checkpw(loginRequest.getPassword(), selectedUser.getPassword())) {
                ctx.cookie("Session", handleLogin(selectedUser.getId()));
            } else {
                ctx.result("Failed to login");
                ctx.status(401);
            }
        });

        app.post("/logout", ctx -> {
            enableAuthCORSFix(ctx);
            String session = ctx.cookie("Session");
            if (tokenUserIdSessionKeeper.remove(session) == null) {
                ctx.status(401);
            } else {
                ctx.status(200);
            }
        });

        app.get("/profile", ctx -> {
            ctx.result(getUserInfo(ctx, factory).getLogin());
        });

        app.post("/register", ctx -> {
            enableAuthCORSFix(ctx);
            var loginRequest = ctx.bodyAsClass(LoginRequest.class);
            var entityManager = factory.createEntityManager();
            try {
                var domainUser = DomainUser.builder().login(loginRequest.getLogin()).password(
                        BCrypt.hashpw(loginRequest.getPassword(), BCrypt.gensalt())
                ).build();
                entityManager.getTransaction().begin();
                entityManager.persist(domainUser);
                entityManager.getTransaction().commit();
            } catch (Exception ex) {
                ctx.result(ex.toString());
                ctx.status(401);
            } finally {
                entityManager.close();
            }
        });
    }

    private static String handleLogin(Integer login) {
        String token = null;
        try {
            token = generateToken();
            tokenUserIdSessionKeeper.put(token, login);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return token;
    }

    private static String generateToken() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        SecretKey secretKey = keyGen.generateKey();
        return DatatypeConverter.printHexBinary(secretKey.getEncoded()).toLowerCase();
    }

    public static DomainUser getUserInfo(Context ctx, EntityManagerFactory factory) {
        String session = ctx.cookie("Session");
        Integer userId = tokenUserIdSessionKeeper.get(session);
        var entityManager = factory.createEntityManager();
        DomainUser domainUser = null;
        try {
            domainUser = entityManager.createQuery("SELECT t FROM User t where t.id=:id", DomainUser.class)
                    .setParameter("id", userId).getSingleResult();
        } catch (Exception ex) {
            ctx.status(401);
        } finally {
            entityManager.close();
        }
        return domainUser;
    }

    public static void enableAuthCORSFix(Context ctx) {
        ctx.header("Access-Control-Allow-Credentials", "true");
    }
}
