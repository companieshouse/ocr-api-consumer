package uk.gov.companieshouse.ocrapiconsumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import uk.gov.companieshouse.ocrapiconsumer.groups.Integration;
import uk.gov.companieshouse.ocrapiconsumer.request.TestParent;

@Integration
@SpringBootTest(classes = OcrApiConsumerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationTest extends TestParent {

    private static final String APPLICATION_URL = "/internal/ocr-requests";
    private static final String HEALTHCHECK_URL = "/internal/ocr-api-consumer/healthcheck";

    @LocalServerPort
    private int port;

    @Autowired
    TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    void verifyControllerReturns202AcceptedWhenCalled() {
        HttpStatus expected = HttpStatus.ACCEPTED;

        String uri = "http://localhost:" + port + APPLICATION_URL;

        String jsonBody =
                "{\n" +
                "  \"image_endpoint\": \"http://testurl.com/cff/servlet/viewArticles?transaction_id=9613245852\",\n" +
                "  \"converted_text_endpoint\": \"http://testurl.com/ocr-results/\",\n" +
                "  \"response_id\": \"9613245852\"\n" +
                "}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        HttpStatus actual = this.restTemplate
                .postForEntity(uri, entity, HttpStatus.class).getStatusCode();

        assertThat(actual, is(expected));
    }

    @Test
    void verifyControllerThrowsRestClientExceptionWhenRequiredBodyMissing() {
        String uri = "http://localhost:" + port + APPLICATION_URL;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        assertThrows(RestClientException.class, () -> this.restTemplate
                .postForEntity(uri, entity, HttpStatus.class));
    }

    @Test
    void verifyControllerThrowsRestClientExceptionWhenPartOfRequiredBodyMissing() {
        String uri = "http://localhost:" + port + APPLICATION_URL;

        String jsonBody =
                "{\n" +
                "  \"image_endpoint\": \"http://testurl.com/cff/servlet/viewArticles?transaction_id=9613245852\",\n" +
                "  \"converted_text_endpoint\": \"http://testurl.com/ocr-results/\"" +
                "}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        assertThrows(RestClientException.class, () -> this.restTemplate
                .postForEntity(uri, entity, HttpStatus.class));
    }

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
