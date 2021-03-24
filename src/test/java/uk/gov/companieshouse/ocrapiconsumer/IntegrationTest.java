package uk.gov.companieshouse.ocrapiconsumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.ocrapiconsumer.groups.Integration;
import uk.gov.companieshouse.ocrapiconsumer.request.TestParent;

@Integration
@SpringBootTest(classes = OcrApiConsumerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationTest extends TestParent {

    private static final String HEALTHCHECK_URL = "/internal/ocr-api-consumer/healthcheck";

    @LocalServerPort
    private int port;

    @Autowired
    TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    void verifyHealthCheckControllerReturns200Alive() {
        HttpStatus expectedResponseCode = HttpStatus.OK;
        String expectedResponseMessage = "ALIVE";

        String uri = "http://localhost:" + port + HEALTHCHECK_URL;

        ResponseEntity<String> response = this.restTemplate.getForEntity(uri, String.class);
        HttpStatus actualResponseCode = response.getStatusCode();
        String actualResponseMessage = response.getBody();

        assertThat(actualResponseCode, is(expectedResponseCode));
        assertThat(actualResponseMessage, is(expectedResponseMessage));
    }

}
