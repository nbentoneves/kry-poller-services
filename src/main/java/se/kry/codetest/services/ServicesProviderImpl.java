package se.kry.codetest.services;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.ResultSet;
import se.kry.codetest.DBConnector;
import se.kry.codetest.domain.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ServicesProviderImpl implements ServicesProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServicesProviderImpl.class);

    private final DBConnector dbConnector;

    public ServicesProviderImpl(DBConnector dbConnector) {
        this.dbConnector = Objects.requireNonNull(dbConnector, "dbConnector can not be null!");
    }

    private static Future<Void> successfully(ResultSet sqlResult) {
        return Future.succeededFuture();
    }

    @Override
    public Future<Set<Service>> getServices() {

        LOGGER.info("opr='getServices', msg='get all services'");

        return dbConnector.query("select * from service")
                .compose(sqlResult -> Future.succeededFuture(
                        sqlResult.getRows().stream()
                                .map(row -> new Service.Builder()
                                        .withUrl(row.getString("url"))
                                        .withAddedDttm(LocalDateTime.parse(row.getString("addedDttm")))
                                        .withStatus(Service.Status.valueOf(row.getString("status")))
                                        .build())
                                .collect(Collectors.toSet())));
    }

    @Override
    public Future<Void> updateService(String url, Service service) {

        LOGGER.info("opr='updateService', msg='Update service'");

        JsonArray queryParams = new JsonArray()
                .add(service.getUrl())
                .add(url);

        return dbConnector.query("update service set url = ? where url = ?", queryParams)
                .compose(ServicesProviderImpl::successfully);
    }

    @Override
    public Future<Void> updateStatusService(Service service) {

        LOGGER.info("opr='updateStatusService', msg='Update service status'");

        JsonArray queryParams = new JsonArray()
                .add(service.getStatus())
                .add(service.getUrl());

        return dbConnector.query("update service set status = ? where url = ?", queryParams)
                .compose(ServicesProviderImpl::successfully);
    }

    @Override
    public Future<Void> addService(Service service) {

        LOGGER.info("opr='addService', msg='Add new service'");

        JsonArray queryParams = new JsonArray()
                .add(service.getUrl())
                .add(DateTimeFormatter.ISO_DATE_TIME.format(service.getAddedDttm()))
                .add(service.getStatus());

        return dbConnector.query("insert into service (url, addedDttm, status) values (?, ?, ?)", queryParams)
                .compose(ServicesProviderImpl::successfully);
    }

    @Override
    public Future<Void> deleteService(String url) {

        LOGGER.info("opr='deleteService', msg='Delete service'");

        JsonArray queryParams = new JsonArray()
                .add(url);

        return dbConnector.query("delete from service where url = ?", queryParams)
                .compose(ServicesProviderImpl::successfully);
    }
}
