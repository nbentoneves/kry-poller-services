package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kry.codetest.services.ServicesProvider;
import se.kry.codetest.services.ServicesProviderImpl;

public class MainVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);
    
    @Override
    public void start(Future<Void> startFuture) {
        String databaseName = System.getProperty("database-name", "poller.db");

        DBConnector connector = new DBConnector(vertx, databaseName);
        ServicesProvider servicesProvider = new ServicesProviderImpl(connector);

        vertx.deployVerticle(new BackgroundPollerVerticle(servicesProvider),
                new DeploymentOptions()
                        .setWorker(true)
                        .setWorkerPoolName("service-update-worker"),
                handler -> {
                    if (handler.succeeded()) {
                        LOGGER.info("Deployed {} with success", BackgroundPollerVerticle.class.getName());
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
