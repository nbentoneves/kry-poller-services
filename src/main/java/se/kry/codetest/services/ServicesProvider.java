package se.kry.codetest.services;

import io.vertx.core.Future;
import se.kry.codetest.domain.Service;

import java.util.Set;

public interface ServicesProvider {

    Future<Set<Service>> getServices();

    Future<Void> updateService(String url, Service service);

    Future<Void> updateStatusService(Service service);

    Future<Void> addService(Service service);

    Future<Void> deleteService(String url);

}
