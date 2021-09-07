package se.kry.codetest;

import com.google.inject.Inject;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kry.codetest.api.ServiceStatusAPI;
import se.kry.codetest.services.ServicesProvider;

import static java.util.Objects.requireNonNull;

public class RestAPIVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    private final ServicesProvider servicesProvider;

    @Inject
    public RestAPIVerticle(ServicesProvider servicesProvider) {
        this.servicesProvider = requireNonNull(servicesProvider, "servicesProvider can not be null!");
    }

    @Override
    public void start(Future<Void> startFuture) {
        Router router = Router.router(vertx);
        ServiceStatusAPI.builder(servicesProvider, router)
                .build();

        vertx.createHttpServer()
                .requestHandler(router)
                .exceptionHandler(ex -> LOGGER.error("Something wrong happened", ex))
                .listen(8080, result -> {
                    if (result.succeeded()) {
                        LOGGER.info("Rest verticle service started");
                        startFuture.complete();
                    } else {
                        startFuture.fail(result.cause());
                    }
                });
    }
}
