package se.kry.codetest.api.handlers;

import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kry.codetest.MainVerticle;
import se.kry.codetest.api.ServiceStatusAPI;
import se.kry.codetest.domain.Error;

public class GenericHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    protected static void setHandlerError(RoutingContext routingContext, Throwable throwable) {
        LOGGER.error("Please check the following problem:", throwable);
        routingContext.response()
                .putHeader("content-type", "application/json")
                .putHeader("Access-Control-Allow-Origin", "*")
                .setStatusCode(500)
                .end(ServiceStatusAPI.GSON.toJson(new Error("Sorry! Something wrong happen...please contact the admin", 500)));
    }

}
