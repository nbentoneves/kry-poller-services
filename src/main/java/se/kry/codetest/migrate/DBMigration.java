package se.kry.codetest.migrate;

import io.vertx.core.Vertx;
import se.kry.codetest.DBConnector;

public class DBMigration {

    public static void createDatabaseTable(Vertx vertx, String databaseName) {
        DBConnector connector = new DBConnector(vertx, databaseName);
        connector.query("CREATE TABLE IF NOT EXISTS service (url VARCHAR(128) NOT NULL PRIMARY KEY, addedDttm VARCHAR(128), status VARCHAR(128))").setHandler(done -> {
            if (done.succeeded()) {
                System.out.println("completed db migrations");
            } else {
                done.cause().printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        createDatabaseTable(vertx, "poller.db");
        vertx.close(shutdown -> {
            System.exit(0);
        });
    }
}
