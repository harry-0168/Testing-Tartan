package tartan.smarthome.db;

import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.Session;
import org.hibernate.SessionException;
import org.hibernate.SessionFactory;
import tartan.smarthome.core.TartanHomeData;
import java.util.List;
import org.hibernate.query.Query;

/**
 * The data access object to log the house data
 */
public class HomeDAO extends AbstractDAO<TartanHomeData> {
    // Keep a reference to the session
    private SessionFactory factory = null;

    public HomeDAO(SessionFactory factory) {
        super(factory);
        this.factory = factory;
    }

    /**
     * Save the taratn home data to the database
     * @param tartanHomeData the data to save
     */
    public void create(TartanHomeData tartanHomeData) {
        try {
            // There is a dropwizard way to establish a Hibernate session outside of Jersey
            // but this is more reliable
            Session session = factory.openSession();
            session.beginTransaction();
            session.save(tartanHomeData);
            session.getTransaction().commit();
            session.close();
        } catch (SessionException sx) {/* Nothing to do */ }
    }

    public SessionFactory getSessionFactory() {
        return this.factory;
    }

    /**
     * Retrieve all TartanHomeData records from the database
     * @return a list of TartanHomeData
     */
    public List<TartanHomeData> findAll() {
        Session session = currentSession();
        Query<TartanHomeData> query = session.createQuery("SELECT t FROM TartanHomeData t", TartanHomeData.class);
        return query.getResultList();
    }
}
