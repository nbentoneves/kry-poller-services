package se.kry.codetest;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kry.codetest.code.BasicModule;

public class MainVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Future<Void> startFuture) {

        Injector injector = Guice.createInjector(new BasicModule(vertx));
        BackgroundPollerVerticle backgroundPollerVerticle = injector.getInstance(BackgroundPollerVerticle.class);
        vertx.deployVerticle(backgroundPollerVerticle,
                new DeploymentOptions()
                        .setWorker(true)
                        .setWorkerPoolSize(1)
                        .setWorkerPoolName("service-update-worker"),
                handler -> {
                    if (handler.succeeded()) {
                        LOGGER.info("Deployed {} with success", BackgroundPollerVerticle.class.getName());
                    } else {
                        LOGGER.info("Failed deployed", handler.cause());
                    }
                }
        );

        vertx.deployVerticle(() -> injector.getInstance(RestAPIVerticle.class),
                //FIXME: How can create multiple instances
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
