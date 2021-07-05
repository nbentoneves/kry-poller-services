package se.kry.codetest;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import se.kry.codetest.domain.Error;
import se.kry.codetest.domain.Service;
import se.kry.codetest.services.ServicesProvider;
import se.kry.codetest.services.ServicesProviderImpl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static se.kry.codetest.migrate.DBMigration.createDatabaseTable;

public class MainVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private final Gson gson = new GsonBuilder().setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context)
                    -> LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_DATE_TIME.withLocale(Locale.ENGLISH)))
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (localDateTime, typeOfT, context)
                    -> new JsonPrimitive(FORMATTER.format(localDateTime)))
            .registerTypeAdapter(Service.class, (InstanceCreator<Service>) (type) -> new Service.Builder().build())
            .create();

    private BackgroundPoller poller;
    private ServicesProvider servicesProvider;
    private DBConnector connector;

    @Override
    public void start(Future<Void> startFuture) {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.Log4j2LogDelegateFactory");
        String databaseName = System.getProperty("database-name", "poller.db");

        createDatabaseTable(vertx, databaseName);

        connector = new DBConnector(vertx, databaseName);
        servicesProvider = new ServicesProviderImpl(connector);
        poller = new BackgroundPoller(servicesProvider);

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        //FIXME: Review this, only enable * because this is an assignment
        router.route().handler(CorsHandler.create("*")
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.OPTIONS)
                .allowedMethod(HttpMethod.PUT)
                .allowedHeader("Access-Control-Request-Method")
                .allowedHeader("Access-Control-Allow-Credentials")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Headers")
                .allowedHeader("Content-Type"));
        setRoutes(router);

        vertx.setPeriodic(1000 * 60, timerId -> poller.pollServices());
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080, result -> {
                    if (result.succeeded()) {
                        LOGGER.info("KRY code test service started");
                        startFuture.complete();
                    } else {
                        startFuture.fail(result.cause());
                    }
                });
        vertx.exceptionHandler(LOGGER::error);
    }

    private void setRoutes(Router router) {

        router.route("/*").handler(StaticHandler.create());
        router.get("/service").handler(this::getServices);
        router.post("/service").handler(this::addService);
        router.put("/service").handler(this::updateService);
        router.delete("/service").handler(this::deleteService);
    }

    private void getServices(RoutingContext routingContext) {
        servicesProvider.getServices()
                .setHandler(handler -> {
                    if (handler.failed()) {
                        setHandlerError(routingContext, handler.cause());
                    } else {
                        routingContext.response()
                                .putHeader("content-type", "application/json")
                                .end(gson.toJson(handler.result()));
                    }
                });
    }

    private void addService(RoutingContext routingContext) {
        Service service = gson.fromJson(routingContext.getBodyAsString(), Service.class);
        servicesProvider.addService(service)
                .setHandler(handler -> {
                    if (handler.failed()) {
                        setHandlerError(routingContext, handler.cause());
                    } else {
                        routingContext.response()
                                .putHeader("content-type", "application/json")
                                .putHeader("Access-Control-Allow-Origin", "*")
                                .end(gson.toJson(ImmutableMap.builder()
                                        .put("result", "OK")
                                        .build()));
                    }
                });
    }

    private void updateService(RoutingContext routingContext) {
        String url = routingContext.request().params().get("name");
        Service service = gson.fromJson(routingContext.getBodyAsString(), Service.class);
        servicesProvider.updateService(url, service)
                .setHandler(handler -> {
                    if (handler.failed()) {
                        setHandlerError(routingContext, handler.cause());
                    } else {
                        routingContext.response()
                                .putHeader("content-type", "application/json")
                                .putHeader("Access-Control-Allow-Origin", "*")
                                .end(gson.toJson(ImmutableMap.builder()
                                        .put("result", "OK")
                                        .build()));
                    }
                });
    }

    private void deleteService(RoutingContext routingContext) {
        String url = routingContext.request().params().get("name");
        servicesProvider.deleteService(url)
                .setHandler(handler -> {
                    if (handler.failed()) {
                        setHandlerError(routingContext, handler.cause());
                    } else {
                        routingContext.response()
                                .putHeader("content-type", "application/json")
                                .putHeader("Access-Control-Allow-Origin", "*")
                                .end(gson.toJson(ImmutableMap.builder()
                                        .put("result", "OK")
                                        .build()));
                    }
                });
    }

    private void setHandlerError(RoutingContext routingContext, Throwable throwable) {
        LOGGER.error("Please check the following problem:", throwable);
        routingContext.response()
                .putHeader("content-type", "application/json")
                .putHeader("Access-Control-Allow-Origin", "*")
                .setStatusCode(500)
                .end(gson.toJson(new Error("Sorry! Something wrong happen...please contact the admin", 500)));
    }


}
