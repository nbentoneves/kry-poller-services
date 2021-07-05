package se.kry.codetest.integration;

import com.google.common.collect.ImmutableMap;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import se.kry.codetest.DBConnector;
import se.kry.codetest.MainVerticle;
import se.kry.codetest.migrate.DBMigration;
import se.kry.codetest.services.ServicesProviderImpl;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.kry.codetest.migrate.DBMigration.createDatabaseTable;

@ExtendWith(VertxExtension.class)
public class MainVerticleIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticleIntegrationTest.class);

    private DBConnector dbConnector;

    @BeforeAll
    static void beforeAll(Vertx vertx) {
        System.setProperty("database-name", "poller-integration-tests.db");
        createDatabaseTable(vertx, "poller-integration-tests.db");
    }

    @AfterEach
    void tearDown() {
        dbConnector.query("delete from service")
                .setHandler(done -> {
                    if (done.succeeded()) {
                        LOGGER.info("Service table cleaned");
                    } else {
                        done.cause().printStackTrace();
                    }
                });
    }

    @BeforeEach
    void deployVerticle(Vertx vertx, VertxTestContext testContext) {
        dbConnector = new DBConnector(vertx, "poller-integration-tests.db");
        vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
    }

    @Test
    @DisplayName("Start a web server and get the services in integration database")
    @Timeout(value = 30, timeUnit = TimeUnit.SECONDS)
    void startHttpServerGetServices(Vertx vertx, VertxTestContext testContext) {

        dbConnector.query("insert into service (url, addedDttm, status) values ('https://google.com', '2021-07-05T15:48:58.377', 'UNKNOWN')")
                .setHandler(done -> {
                    if (done.succeeded()) {
                        LOGGER.info("Service inserted");
                    } else {
                        done.cause().printStackTrace();
                    }
                });

        WebClient.create(vertx)
                .get(8080, "::1", "/service")
                .send(response -> testContext.verify(() -> {
                    then(response.result().statusCode()).isEqualTo(200);
                    then(response.result().bodyAsJsonArray()).hasSize(1);
                    then(response.result().bodyAsJsonArray().getJsonObject(0).getString("name")).isEqualTo("https://google.com");
                    then(response.result().bodyAsJsonArray().getJsonObject(0).getString("addedDttm")).isEqualTo("2021-07-05T15:48:58.377");
                    then(response.result().bodyAsJsonArray().getJsonObject(0).getString("status")).isEqualTo("UNKNOWN");
                    testContext.completeNow();
                }));
    }

    @Test
    @DisplayName("Start a web server and update service in integration database")
    @Timeout(value = 30, timeUnit = TimeUnit.SECONDS)
    void startHttpServerUpdateServices(Vertx vertx, VertxTestContext testContext) {

        dbConnector.query("insert into service (url, addedDttm, status) values ('https://google.com', '2021-07-05T15:48:58.377', 'UNKNOWN')")
                .setHandler(done -> {
                    if (done.succeeded()) {
                        LOGGER.info("Service inserted");
                    } else {
                        done.cause().printStackTrace();
                    }
                });

        JsonObject body = new JsonObject(ImmutableMap
                .of("name", "https://google2.com"));

        WebClient webClient = WebClient.create(vertx);

        webClient.put(8080, "::1", "/service?name=https://google.com")
                .sendJsonObject(body, response -> testContext.verify(() -> {
                    then(response.result().statusCode()).isEqualTo(200);
                    then(response.result().bodyAsJsonObject().getString("result")).isEqualTo("OK");
                }));

        webClient.get(8080, "::1", "/service")
                .send(response -> testContext.verify(() -> {
                    then(response.result().statusCode()).isEqualTo(200);
                    then(response.result().bodyAsJsonArray()).hasSize(1);
                    then(response.result().bodyAsJsonArray().getJsonObject(0).getString("name")).isEqualTo("https://google2.com");
                    then(response.result().bodyAsJsonArray().getJsonObject(0).getString("addedDttm")).isEqualTo("2021-07-05T15:48:58.377");
                    then(response.result().bodyAsJsonArray().getJsonObject(0).getString("status")).isEqualTo("UNKNOWN");
                    testContext.completeNow();
                }));
    }

    @Test
    @DisplayName("Start a web server and add new service in integration database")
    @Timeout(value = 30, timeUnit = TimeUnit.SECONDS)
    void startHttpServerAddNewServices(Vertx vertx, VertxTestContext testContext) {

        dbConnector.query("insert into service (url, addedDttm, status) values ('https://google.com', '2021-07-05T15:48:58.377', 'UNKNOWN')")
                .setHandler(done -> {
                    if (done.succeeded()) {
                        LOGGER.info("Service inserted");
                    } else {
                        done.cause().printStackTrace();
                    }
                });

        JsonObject body = new JsonObject(ImmutableMap
                .of("name", "https://vertx.com"));

        WebClient webClient = WebClient.create(vertx);

        webClient.post(8080, "::1", "/service")
                .sendJsonObject(body, response -> testContext.verify(() -> {
                    then(response.result().statusCode()).isEqualTo(200);
                    then(response.result().bodyAsJsonObject().getString("result")).isEqualTo("OK");
                }));

        webClient.get(8080, "::1", "/service")
                .send(response -> testContext.verify(() -> {
                    then(response.result().statusCode()).isEqualTo(200);
                    then(response.result().bodyAsJsonArray()).hasSize(2);

                    Optional<Object> service = response.result().bodyAsJsonArray()
                            .stream()
                            .filter(object -> ((JsonObject) object).getString("name").equals("https://vertx.com"))
                            .findFirst();

                    then(service.isPresent()).isTrue();
                    testContext.completeNow();
                }));
    }

    @Test
    @DisplayName("Start a web server and delete service in integration database")
    @Timeout(value = 30, timeUnit = TimeUnit.SECONDS)
    void startHttpServerDeleteServices(Vertx vertx, VertxTestContext testContext) {

        dbConnector.query("insert into service (url, addedDttm, status) values ('https://google.com', '2021-07-05T15:48:58.377', 'UNKNOWN')")
                .setHandler(done -> {
                    if (done.succeeded()) {
                        LOGGER.info("Service inserted");
                    } else {
                        done.cause().printStackTrace();
                    }
                });

        WebClient webClient = WebClient.create(vertx);

        webClient.delete(8080, "::1", "/service?name=https://google.com")
                .send(response -> testContext.verify(() -> {
                    then(response.result().statusCode()).isEqualTo(200);
                    then(response.result().bodyAsJsonObject().getString("result")).isEqualTo("OK");
                }));

        webClient.get(8080, "::1", "/service")
                .send(response -> testContext.verify(() -> {
                    then(response.result().statusCode()).isEqualTo(200);
                    then(response.result().bodyAsJsonArray()).hasSize(0);
                    testContext.completeNow();
                }));
    }

    @Test
    @DisplayName("Start a web server and poller update service status in integration database")
    @Timeout(value = 30, timeUnit = TimeUnit.SECONDS)
    void startHttpServerPollerServices(Vertx vertx, VertxTestContext testContext) throws InterruptedException {

        dbConnector.query("insert into service (url, addedDttm, status) values ('https://google.com', '2021-07-05T15:48:58.377', 'UNKNOWN')")
                .setHandler(done -> {
                    if (done.succeeded()) {
                        LOGGER.info("Service inserted");
                    } else {
                        done.cause().printStackTrace();
                    }
                });

        WebClient webClient = WebClient.create(vertx);

        //Wait for the first poller call
        TimeUnit.SECONDS.sleep(65);

        webClient.get(8080, "::1", "/service")
                .send(response -> testContext.verify(() -> {
                    then(response.result().statusCode()).isEqualTo(200);
                    then(response.result().bodyAsJsonArray()).hasSize(1);
                    then(response.result().bodyAsJsonArray().getJsonObject(0).getString("name")).isEqualTo("https://google.com");
                    then(response.result().bodyAsJsonArray().getJsonObject(0).getString("addedDttm")).isEqualTo("2021-07-05T15:48:58.377");
                    then(response.result().bodyAsJsonArray().getJsonObject(0).getString("status")).isEqualTo("OK");
                    testContext.completeNow();
                }));
    }

}
