package se.kry.codetest.util;

import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kry.codetest.MainVerticle;
import se.kry.codetest.domain.Service;

import java.net.HttpURLConnection;
import java.net.URL;

public class PingStatus {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);
    private static final int TIMEOUT = 3000;

    public static Future<Service.Status> getStatus(String url) {

        Future<Service.Status> resultStatus;

        try {

            URL site = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) site.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT);
            connection.connect();

            if (connection.getResponseCode() == 500) {
                resultStatus = Future.succeededFuture(Service.Status.FAIL);
            } else {
                resultStatus = Future.succeededFuture(Service.Status.OK);
            }
        } catch (Exception ex) {
            LOGGER.error("Problem trying to get the URL status", ex);
            resultStatus = Future.succeededFuture(Service.Status.UNKNOWN);
        }

        return resultStatus;
    }

}
