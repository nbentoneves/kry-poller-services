package se.kry.codetest;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kry.codetest.code.BasicModule;
import se.kry.codetest.services.ServicesProvider;

import java.util.function.Supplier;

public class MainVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Future<Void> startFuture) {

        Injector injector = Guice.createInjector(new BasicModule(vertx));
        ServicesProvider servicesProvider = injector.getInstance(ServicesProvider.class);
        BackgroundPollerVerticle backgroundPollerVerticle = injector.getInstance(BackgroundPollerVerticle.class);
        RestAPIVerticle restAPIVerticle = injector.getInstance(RestAPIVerticle.class);

        vertx.deployVerticle(backgroundPollerVerticle,
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

        vertx.deployVerticle(restAPIVerticle,
                //FIXME: How can create multiple instances
                //new DeploymentOptions()
                //        .setInstances(Runtime.getRuntime().availableProcessors()),
                handler -> {
                    if (handler.succeeded()) {
                        LOGGER.info("Deployed {} with success", RestAPIVerticle.class.getName());
                    } else {
                        LOGGER.info("Failed deployed", handler.cause());
                    }
                });
    }

}
