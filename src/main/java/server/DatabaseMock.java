package server;

import domain.DomainBoard;
import domain.DomainCard;
import domain.DomainList;
import domain.DomainUser;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;

public class DatabaseMock {
    //Mock do sprawdzenia czy baza dziala
    public static void createMock(EntityManagerFactory factory) {

        EntityManager manager = factory.createEntityManager();
        manager.getTransaction().begin();

        List<DomainList> listCollection = new ArrayList<>();
        List<DomainCard> cardCollection = new ArrayList<>();
        cardCollection.add(DomainCard.builder().name("TEST1").value("kartaJedna").build());
        cardCollection.add(DomainCard.builder().name("TEST2").value("kartaDruga").build());
        listCollection.add(DomainList.builder().name("Lista1").cards(cardCollection).build());
        cardCollection = new ArrayList<>();
        cardCollection.add(DomainCard.builder().name("TEST1").value("kartaJedna").build());
        cardCollection.add(DomainCard.builder().name("TEST2").value("kartaDruga").build());
        listCollection.add(DomainList.builder().name("Lista2").cards(cardCollection).build());
        cardCollection = new ArrayList<>();
        cardCollection.add(DomainCard.builder().name("TEST1").value("kartaJedna").build());
        cardCollection.add(DomainCard.builder().name("TEST2").value("kartaDruga").build());
        listCollection.add(DomainList.builder().name("Lista3").cards(cardCollection).build());
        cardCollection = new ArrayList<>();
        cardCollection.add(DomainCard.builder().name("TEST1").value("kartaJedna").build());
        cardCollection.add(DomainCard.builder().name("TEST2").value("kartaDruga").build());
        listCollection.add(DomainList.builder().name("Lista4").cards(cardCollection).build());
        DomainBoard table = DomainBoard.builder().name("TESTOWA TABLICA").lists(listCollection).build();

        manager.persist(table);

        listCollection = new ArrayList<>();
        cardCollection = new ArrayList<>();
        cardCollection.add(DomainCard.builder().name("TEST1").value("kartaJedna").build());
        cardCollection.add(DomainCard.builder().name("TEST2").value("kartaDruga").build());
        listCollection.add(DomainList.builder().name("Lista5").cards(cardCollection).build());
        cardCollection = new ArrayList<>();
        cardCollection.add(DomainCard.builder().name("TEST1").value("kartaJedna").build());
        cardCollection.add(DomainCard.builder().name("TEST2").value("kartaDruga").build());
        listCollection.add(DomainList.builder().name("Lista6").cards(cardCollection).build());
        cardCollection = new ArrayList<>();
        cardCollection.add(DomainCard.builder().name("TEST1").value("kartaJedna").build());
        cardCollection.add(DomainCard.builder().name("TEST2").value("kartaDruga").build());
        listCollection.add(DomainList.builder().name("Lista7").cards(cardCollection).build());
        cardCollection = new ArrayList<>();
        cardCollection.add(DomainCard.builder().name("TEST1").value("kartaJedna").build());
        cardCollection.add(DomainCard.builder().name("TEST2").value("kartaDruga").build());
        listCollection.add(DomainList.builder().name("Lista8").cards(cardCollection).build());
        DomainBoard table2 = DomainBoard.builder().name("TESTOWA TABLICA 2").lists(listCollection).build();

        manager.persist(table2);

        DomainUser domainUser = DomainUser.builder().login("test").password(BCrypt.hashpw("123", BCrypt.gensalt())).build();
        manager.persist(domainUser);
        DomainUser domainUser1 = DomainUser.builder().login("user").password(BCrypt.hashpw("123", BCrypt.gensalt())).build();
        manager.persist(domainUser1);

        manager.getTransaction().commit();
        manager.close();
    }
}
