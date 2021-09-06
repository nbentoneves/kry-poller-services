package se.kry.codetest.api.handlers;

import com.google.common.collect.ImmutableMap;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import se.kry.codetest.api.ServiceStatusAPI;
import se.kry.codetest.domain.Service;
import se.kry.codetest.services.ServicesProvider;

import static com.google.common.base.Preconditions.checkNotNull;
import static se.kry.codetest.api.handlers.GenericHandler.setHandlerError;

public class AddServicesHandler implements Handler<RoutingContext> {

    private ServicesProvider servicesProvider;

    public AddServicesHandler(ServicesProvider servicesProvider) {
        checkNotNull(servicesProvider, "servicesProvider can not be null");
        this.servicesProvider = servicesProvider;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        Service service = ServiceStatusAPI.GSON.fromJson(routingContext.getBodyAsString(), Service.class);
        servicesProvider.addService(service)
                .setHandler(handler -> {
                    if (handler.failed()) {
                        setHandlerError(routingContext, handler.cause());
                    } else {
                        routingContext.response()
                                .putHeader("content-type", "application/json")
                                .putHeader("Access-Control-Allow-Origin", "*")
                                .end(ServiceStatusAPI.GSON.toJson(ImmutableMap.builder()
                                        .put("result", "OK")
                                        .build()));
                    }
                });
    }
}