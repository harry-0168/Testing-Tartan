package tartan.smarthome;

import io.dropwizard.core.Application;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import org.hibernate.Session;
import java.io.File;
import java.util.Map;

import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.views.common.ViewBundle;
import tartan.smarthome.auth.TartanAuthenticator;
import tartan.smarthome.auth.TartanUser;
import tartan.smarthome.core.TartanHomeData;
import tartan.smarthome.db.HomeDAO;
import tartan.smarthome.resources.TartanResource;

/**
 * This is the driver for the program.
 * @see <a href="https://www.dropwizard.io/1.0.0/docs/manual/core.html#application">Dropwizard Applications</a>
 */
public class TartanHomeApplication extends Application<TartanHomeConfiguration> {

    private final HibernateBundle<TartanHomeConfiguration> hibernateBundle =
            new HibernateBundle<TartanHomeConfiguration>(TartanHomeData.class) {
                @Override
                public DataSourceFactory getDataSourceFactory(TartanHomeConfiguration configuration) {
                    return configuration.getDataSourceFactory();
                }
            };

    /**
     * The driver
     * @param args command line arguments
     * @throws Exception a catch all exception
     */
    public static void main(final String[] args) throws Exception {
        new TartanHomeApplication().run(args);
    }

    /**
     * Get the application name. This is the core URL for the system
     * @return the name
     */
    @Override
    public String getName() {
        return "smarthome";
    }

    /**
     * Initialize the system
     * @param bootstrap the initial settings from from the YAML file
     */
    @Override
    public void initialize(final Bootstrap<TartanHomeConfiguration> bootstrap) {
        // We need the view bundle for rendering
        bootstrap.addBundle(new ViewBundle());
        bootstrap.addBundle(hibernateBundle);
    }

    /**
     * Run the system.
     * @param configuration system settings
     * @param environment system environment
     */
    @Override
    public void run(final TartanHomeConfiguration configuration,
                    final Environment environment) {
        HomeDAO dao = new HomeDAO(hibernateBundle.getSessionFactory());

        TartanAuthenticator auth = new TartanAuthenticator();
        auth.setValidUsers(configuration);

        final TartanResource resource = new TartanResource(configuration.getHouses(),
                dao, Integer.parseInt(configuration.getHistoryTimer()));

        environment.jersey().register(resource);
        environment.jersey().register(new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<TartanUser>()
                .setAuthenticator(auth)
                .buildAuthFilter()));
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(TartanUser.class));

        scheduleDailyReport(dao, environment);
    }

    // This method sets up a daily job. For quick tests, set it to 1 minute.
    private void scheduleDailyReport(HomeDAO dao, Environment environment) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        // For real usage, set initialDelay=0, period=24, and TimeUnit.HOURS.
        // For quick testing, use period=1 and TimeUnit.MINUTES or SECONDS.
        scheduler.scheduleAtFixedRate(() -> {
            try {
                generateReport(dao);
            } catch (Exception e) {
                // Log or handle
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    private void generateReport(HomeDAO dao) {
        System.out.println("Running report job...");

        try (Session session = dao.getSessionFactory().openSession()) {
            session.beginTransaction();
            
            // 1) Query all records from DB
            List<TartanHomeData> dataList = session.createQuery("FROM TartanHomeData", TartanHomeData.class).list();
            session.getTransaction().commit();
            
            if (dataList == null || dataList.isEmpty()) {
                System.out.println("No data found, skipping report generation.");
                return;
            }
            
            // 2) Filter: for each house, keep only the latest record (based on createTimeStamp)
            Map<String, TartanHomeData> latestRecords = new HashMap<>();
            for (TartanHomeData record : dataList) {
                String houseName = record.getHomeName();
                if (houseName == null) continue;
                if (!latestRecords.containsKey(houseName) ||
                    record.getCreateTimeStamp().after(latestRecords.get(houseName).getCreateTimeStamp())) {
                    latestRecords.put(houseName, record);
                }
            }
            
            // 3) Generate a report for each house based on the AB testing format.
            for (TartanHomeData record : latestRecords.values()) {
                String houseName = record.getHomeName();
                String fileName = "report-" + LocalDate.now() + "-" + houseName + ".csv";
                String groupExperiment = record.getGroupExperiment();
                // minutesLightsOn stored as a long (millis)
                long minutesLightsOn = record.getMinutesLightsOn() != null ? record.getMinutesLightsOn() : 0L;
                File outputFile = new File("/tmp", fileName);
                
                if ("1".equals(groupExperiment)) {
                    // For group 1, display usage as minutes and seconds.
                    long minutes = (minutesLightsOn / (60 * 1000));
                    long seconds = (minutesLightsOn / 1000) % 60;
                    try (FileWriter fw = new FileWriter(outputFile)) {
                        fw.write("House Name, Light Usage Minute, Light Usage Second\n");
                        fw.write(houseName + "," + minutes + " minutes, " + seconds + " seconds\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.err.println("Failed to write daily report: " + e.getMessage());
                    }
                } else if ("2".equals(groupExperiment)) {
                    // For group 2, display estimated cost.
                    long minutes = (minutesLightsOn / (60 * 1000));
                    long seconds = (minutesLightsOn / 1000) % 60;
                    double cost = (minutesLightsOn / (60.0 * 1000.0)) * 0.05;
                    try (FileWriter fw = new FileWriter(outputFile)) {
                        fw.write("House Name, Light Usage Minute, Light Usage Second, Estimated Cost\n");
                        fw.write(houseName + "," + minutes + " minutes, " + seconds + " seconds, $" + cost + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.err.println("Failed to write daily report: " + e.getMessage());
                    }
                }
            }
            
            System.out.println("Daily report generated!");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error generating report: " + e.getMessage());
        }
    }


    
}
