package uk.gov.companieshouse.ocrapiconsumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import uk.gov.companieshouse.ocrapiconsumer.groups.Integration;
import uk.gov.companieshouse.ocrapiconsumer.request.TestParent;

@Integration
@SpringBootTest(classes = OcrApiConsumerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationTest extends TestParent {

    private static final String IMAGE_ENDPOINT_PARAM_NAME = "image_endpoint";
    private static final String CONVERTED_TEXT_ENDPOINT_PARAM_NAME = "converted_text_endpoint";
    private static final String RESPONSE_ID_PARAM_NAME = "response_id";
    private static final String APPLICATION_URL = "/internal/ocr-requests";
    private static final String HEALTHCHECK_URL = "/internal/ocr-api-consumer/healthcheck";

    @LocalServerPort
    private int port;

    @Autowired
    TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    void verifyControllerReturns202AcceptedWhenCalled() {
        HttpStatus expected = HttpStatus.ACCEPTED;

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add(IMAGE_ENDPOINT_PARAM_NAME, IMAGE_ENDPOINT);
        params.add(CONVERTED_TEXT_ENDPOINT_PARAM_NAME, CONVERTED_TEXT_ENDPOINT);
        params.add(RESPONSE_ID_PARAM_NAME, RESPONSE_ID);

        String uri = "http://localhost:" + port + APPLICATION_URL;

        HttpStatus actual = this.restTemplate
                .postForEntity(uri, params, HttpStatus.class).getStatusCode();

        assertThat(actual, is(expected));
    }

    @Test
    void verifyControllerThrowsRestClientExceptionWhenRequiredParameterMissing() {

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add(RESPONSE_ID_PARAM_NAME, RESPONSE_ID);
        params.add(IMAGE_ENDPOINT_PARAM_NAME, IMAGE_ENDPOINT);
        // no extracted text endpoint parameter which is a required parameter

        String uri = "http://localhost:" + port + APPLICATION_URL;
        assertThrows(RestClientException.class, () -> this.restTemplate
                .postForEntity(uri, params, HttpStatus.class));
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
