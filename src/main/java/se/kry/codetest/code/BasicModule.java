package se.kry.codetest.code;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletScopes;
import io.vertx.core.Vertx;
import se.kry.codetest.BackgroundPollerVerticle;
import se.kry.codetest.DBConnector;
import se.kry.codetest.RestAPIVerticle;
import se.kry.codetest.services.ServicesProvider;
import se.kry.codetest.services.ServicesProviderImpl;

import static se.kry.codetest.migrate.DBMigration.createDatabaseTable;

public class BasicModule extends AbstractModule {

    private final Vertx vertx;

    public BasicModule(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    protected void configure() {
        String databaseName = System.getProperty("database-name", "poller.db");
        createDatabaseTable(vertx, databaseName);

        bind(DBConnector.class).toInstance(new DBConnector(vertx, databaseName));
        bind(ServicesProvider.class).to(ServicesProviderImpl.class);
        bind(BackgroundPollerVerticle.class);
        bind(RestAPIVerticle.class);
    }
}
