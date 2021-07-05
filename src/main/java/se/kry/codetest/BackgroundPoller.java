package se.kry.codetest;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import se.kry.codetest.domain.Service;
import se.kry.codetest.services.ServicesProvider;
import se.kry.codetest.util.PingStatus;

import java.util.Objects;

public class BackgroundPoller {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    private final ServicesProvider servicesProvider;

    public BackgroundPoller(ServicesProvider servicesProvider) {
        this.servicesProvider = Objects.requireNonNull(servicesProvider, "servicesProvider can not be null!");
    }

    public void pollServices() {
        LOGGER.info("Starting poll services to update the services status...");

        servicesProvider.getServices()
                .setHandler(result -> {
                    for (Service service : result.result()) {
                        PingStatus.getStatus(service.getUrl())
                                .compose(status -> {
                                    Service newService = new Service.Builder()
                                            .withUrl(service.getUrl())
                                            .withAddedDttm(service.getAddedDttm())
                                            .withStatus(status)
                                            .build();

                                    return servicesProvider.updateStatusService(newService);
                                });
                    }
                });
    }


}
