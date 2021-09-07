package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kry.codetest.domain.Service;
import se.kry.codetest.services.ServicesProvider;
import se.kry.codetest.util.PingStatus;

import static java.util.Objects.requireNonNull;

public class BackgroundPollerVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    private final ServicesProvider servicesProvider;

    public BackgroundPollerVerticle(ServicesProvider servicesProvider) {
        this.servicesProvider = requireNonNull(servicesProvider, "servicesProvider can not be null!");
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        startFuture.complete();
        LOGGER.info("Started BackgroundPoller verticle");
        vertx.setPeriodic(1000 * 20, timerId -> pollServices());
    }

    public void pollServices() {
        LOGGER.info("Starting poll services to update the services status...");

        servicesProvider.getServices()
                .setHandler(result -> {
                    for (Service service : result.result()) {

                        Service.Status status = PingStatus.getStatus(service.getUrl());
                        Service newService = new Service.Builder()
                                .withUrl(service.getUrl())
                                .withAddedDttm(service.getAddedDttm())
                                .withStatus(status)
                                .build();

                        servicesProvider.updateStatusService(newService);
                    }
                });
    }
}
