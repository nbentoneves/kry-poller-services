package se.kry.codetest;

import io.vertx.core.Vertx;

import static se.kry.codetest.migrate.DBMigration.createDatabaseTable;

public class Start {

    public static void main(String[] args) {

        Vertx vertx = Vertx.vertx();

        String databaseName = System.getProperty("database-name", "poller.db");

        createDatabaseTable(vertx, databaseName);

        vertx.deployVerticle(new MainVerticle());
    }
}
