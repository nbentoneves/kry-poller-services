package se.kry.codetest.api.handlers;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import se.kry.codetest.api.ServiceStatusAPI;
import se.kry.codetest.services.ServicesProvider;

import static com.google.common.base.Preconditions.checkNotNull;
import static se.kry.codetest.api.handlers.GenericHandler.setHandlerError;

public class GetServicesHandler implements Handler<RoutingContext> {

    private ServicesProvider servicesProvider;

    public GetServicesHandler(ServicesProvider servicesProvider) {
        checkNotNull(servicesProvider, "servicesProvider can not be null");
        this.servicesProvider = servicesProvider;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        servicesProvider.getServices()
                .setHandler(handler -> {
                    if (handler.failed()) {
                        setHandlerError(routingContext, handler.cause());
                    } else {
                        routingContext.response()
                                .putHeader("content-type", "application/json")
                                .end(ServiceStatusAPI.GSON.toJson(handler.result()));
                    }
                });
    }
}
