package se.kry.codetest.services;


import com.google.common.collect.ImmutableList;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.kry.codetest.DBConnector;
import se.kry.codetest.domain.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.of;
import static io.vertx.core.Future.succeededFuture;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ServicesProviderImplTest {

    @Mock
    private DBConnector dbConnector;

    @Mock
    private ResultSet resultSet;

    private final JsonObject siteGoogle = new JsonObject(of(
            "url", "http://google.com",
            "addedDttm", "2021-07-05T15:48:58.377",
            "status", "UNKNOWN"));

    private final JsonObject siteVert = new JsonObject(of(
            "url", "http://vert.com",
            "addedDttm", "2021-07-05T15:48:58.377",
            "status", "UNKNOWN"));

    private ServicesProvider servicesProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        servicesProvider = new ServicesProviderImpl(dbConnector);
    }

    @Test
    void testGetServicesSuccessfully() {

        List<JsonObject> objects = ImmutableList
                .<JsonObject>builder()
                .add(siteGoogle)
                .add(siteVert)
                .build();

        doReturn(succeededFuture(resultSet)).when(dbConnector).query(any());
        doReturn(objects).when(resultSet).getRows();

        Future<Set<Service>> result = servicesProvider.getServices();

        verify(dbConnector, times(1)).query("select * from service");

        then(result).isNotNull();
        then(result.result()).hasSize(2);

        Optional<Service> service = result.result().stream()
                .filter(it -> it.getUrl().equals("http://google.com"))
                .findFirst();

        then(service.isPresent()).isTrue();
        then(service.get().getAddedDttm()).isEqualTo(LocalDateTime.parse("2021-07-05T15:48:58.377"));
        then(service.get().getStatus()).isEqualTo(Service.Status.UNKNOWN);

    }

    @Test
    void testGetServicesFailed() {

        doReturn(Future.failedFuture(new Exception("Problem"))).when(dbConnector).query(any());

        Future<Set<Service>> result = servicesProvider.getServices();

        verify(dbConnector, times(1)).query("select * from service");

        then(result).isNotNull();
        then(result.failed()).isTrue();

    }

    @Test
    void testUpdateServicesSuccessfully() {

        Service service = new Service.Builder()
                .withUrl("http://new-google.com")
                .withAddedDttm(LocalDateTime.now())
                .withStatus(Service.Status.UNKNOWN)
                .build();

        JsonArray queryParamsExpected = new JsonArray()
                .add("http://new-google.com")
                .add("http://google.com");

        doReturn(succeededFuture(resultSet)).when(dbConnector).query(any(), any());

        Future<Void> result = servicesProvider.updateService("http://google.com", service);

        verify(dbConnector, times(1)).query("update service set url = ? where url = ?", queryParamsExpected);

        then(result).isNotNull();
        then(result.succeeded()).isTrue();

    }


    @Test
    void testAddServicesSuccessfully() {

        Service service = new Service.Builder()
                .withUrl("http://google.com")
                .withAddedDttm(LocalDateTime.parse("2021-07-05T15:48:58.377"))
                .withStatus(Service.Status.UNKNOWN)
                .build();

        JsonArray queryParamsExpected = new JsonArray()
                .add("http://google.com")
                .add("2021-07-05T15:48:58.377")
                .add("UNKNOWN");

        doReturn(succeededFuture(resultSet)).when(dbConnector).query(any(), any());

        Future<Void> result = servicesProvider.addService(service);

        verify(dbConnector, times(1)).query("insert into service (url, addedDttm, status) values (?, ?, ?)", queryParamsExpected);

        then(result).isNotNull();
        then(result.succeeded()).isTrue();

    }

    @Test
    void testDeleteServicesSuccessfully() {

        JsonArray queryParamsExpected = new JsonArray()
                .add("http://google.com");

        doReturn(succeededFuture(resultSet)).when(dbConnector).query(any(), any());

        Future<Void> result = servicesProvider.deleteService("http://google.com");

        verify(dbConnector, times(1)).query("delete from service where url = ?", queryParamsExpected);

        then(result).isNotNull();
        then(result.succeeded()).isTrue();

    }

}
