package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kry.codetest.api.ServiceStatusAPI;
import se.kry.codetest.services.ServicesProviderImpl;

public class RestAPIVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Future<Void> startFuture) {
        String databaseName = System.getProperty("database-name", "poller.db");

        DBConnector connector = new DBConnector(vertx, databaseName);
        ServicesProviderImpl servicesProvider = new ServicesProviderImpl(connector);

        Router router = Router.router(vertx);
        ServiceStatusAPI.builder(servicesProvider, router)
                .build();

        vertx.createHttpServer()
                .requestHandler(router)
                .exceptionHandler(ex -> LOGGER.error("Something wrong happened", ex))
                .listen(8080, result -> {
                    if (result.succeeded()) {
                        LOGGER.info("KRY code test service started");
                        startFuture.complete();
                    } else {
                        startFuture.fail(result.cause());
                    }
                });
    }
}
