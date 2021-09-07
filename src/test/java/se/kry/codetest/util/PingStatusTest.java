package se.kry.codetest.util;

import io.vertx.core.Future;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import se.kry.codetest.domain.Service;
import se.kry.codetest.util.PingStatus;

import static org.assertj.core.api.BDDAssertions.then;

public class PingStatusTest {

    @Test
    void testGetOkStatus() {

        Service.Status result = PingStatus.getStatus("http://google.com");
        then(result).isEqualTo(Service.Status.OK);

    }

    @Test
    void testGetUnknownStatus() {

        Service.Status result = PingStatus.getStatus("invalid");
        then(result).isEqualTo(Service.Status.UNKNOWN);

    }

    //FIXME: Change the PingStatus to inject the HttpURLConnection and mock the instance
    @Test
    @Disabled
    void testGetFailedStatus() {

        Service.Status result = PingStatus.getStatus("http://google.com");
        then(result).isEqualTo(Service.Status.FAIL);

    }

}
