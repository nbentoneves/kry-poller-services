package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kry.codetest.services.ServicesProvider;
import se.kry.codetest.services.ServicesProviderImpl;

import static se.kry.codetest.migrate.DBMigration.createDatabaseTable;

public class MainVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    private BackgroundPoller poller;
    private ServicesProvider servicesProvider;
    private DBConnector connector;

    @Override
    public void start(Future<Void> startFuture) {
        String databaseName = System.getProperty("database-name", "poller.db");

        createDatabaseTable(vertx, databaseName);

        connector = new DBConnector(vertx, databaseName);
        servicesProvider = new ServicesProviderImpl(connector);
        poller = new BackgroundPoller(servicesProvider);

        vertx.deployVerticle(new BackgroundPoller(servicesProvider),
                new DeploymentOptions()
                        .setWorker(true)
                        .setWorkerPoolName("service-update-worker"),
                handler -> {
                    if (handler.succeeded()) {
                        LOGGER.info("Deployed {} with success", BackgroundPoller.class.getName());
                    } else {
                        LOGGER.info("Failed deployed", handler.cause());
                    }
                }
        );

        vertx.deployVerticle(RestAPIVerticle.class.getName(),
                new DeploymentOptions()
                        .setInstances(Runtime.getRuntime().availableProcessors()),
                handler -> {
                    if (handler.succeeded()) {
                        LOGGER.info("Deployed {} with success", RestAPIVerticle.class.getName());
                    } else {
                        LOGGER.info("Failed deployed", handler.cause());
                    }
                });
    }

}
