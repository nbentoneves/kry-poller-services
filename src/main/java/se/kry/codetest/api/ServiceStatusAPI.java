package se.kry.codetest.api;

import com.google.gson.*;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kry.codetest.MainVerticle;
import se.kry.codetest.api.handlers.AddServicesHandler;
import se.kry.codetest.api.handlers.DeleteServicesHandler;
import se.kry.codetest.api.handlers.GetServicesHandler;
import se.kry.codetest.api.handlers.UpdateServicesHandler;
import se.kry.codetest.domain.Service;
import se.kry.codetest.services.ServicesProvider;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ServiceStatusAPI {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context)
                    -> LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_DATE_TIME.withLocale(Locale.ENGLISH)))
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (localDateTime, typeOfT, context)
                    -> new JsonPrimitive(FORMATTER.format(localDateTime)))
            .registerTypeAdapter(Service.class, (InstanceCreator<Service>) (type) -> new Service.Builder().build())
            .create();

    private final ServicesProvider servicesProvider;

    private ServiceStatusAPI(Builder builder) {
        this.servicesProvider = builder.servicesProvider;

        builder.router.route()
                .handler(BodyHandler.create());

        builder.router.route()
                //FIXME: Review this, only enable * because this is an assignment
                .handler(CorsHandler.create("*")
                        .allowedMethod(HttpMethod.GET)
                        .allowedMethod(HttpMethod.POST)
                        .allowedMethod(HttpMethod.OPTIONS)
                        .allowedMethod(HttpMethod.PUT)
                        .allowedMethod(HttpMethod.DELETE)
                        .allowedHeader("Access-Control-Request-Method")
                        .allowedHeader("Access-Control-Allow-Credentials")
                        .allowedHeader("Access-Control-Allow-Origin")
                        .allowedHeader("Access-Control-Allow-Headers")
                        .allowedHeader("Content-Type"));
        builder.router.route().failureHandler(errorContext -> {
            if (!errorContext.response().ended()) {
                LOGGER.error("Router error: ", errorContext.failure());
                errorContext.response()
                        .setStatusCode(500)
                        .end(new JsonObject().put("message", "Something went wrong").toBuffer());
            }
        });

        builder.router.route("/*").handler(StaticHandler.create());
        builder.router.get("/service").handler(new GetServicesHandler(servicesProvider));
        builder.router.post("/service").handler(new AddServicesHandler(servicesProvider));
        builder.router.put("/service").handler(new UpdateServicesHandler(servicesProvider));
        builder.router.delete("/service").handler(new DeleteServicesHandler(servicesProvider));
    }

    public static Builder builder(ServicesProvider servicesProvider, Router router) {
        return new Builder(servicesProvider, router);
    }

    public static class Builder {

        private final ServicesProvider servicesProvider;
        private final Router router;

        public Builder(ServicesProvider servicesProvider, Router router) {
            checkNotNull(servicesProvider, "servicesProvider can not be null");
            checkNotNull(router, "router can not be null");
            this.servicesProvider = servicesProvider;
            this.router = router;
        }

        public void build() {
            new ServiceStatusAPI(this);
        }

    }

}
