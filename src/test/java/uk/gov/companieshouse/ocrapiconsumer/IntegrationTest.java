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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import uk.gov.companieshouse.ocrapiconsumer.groups.Integration;
import uk.gov.companieshouse.ocrapiconsumer.request.TestParent;

@Integration
@SpringBootTest(classes = OcrApiConsumerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationTest extends TestParent {

    private static final String EXTERNAL_REFERENCE_ID_PARAM_NAME = "external-reference-id";
    private static final String IMAGE_ENDPOINT_PARAM_NAME = "image-endpoint";
    private static final String EXTRACTED_TEXT_ENDPOINT_PARAM_NAME = "extracted-text-endpoint";
    private static final String APPLICATION_URL = "/ocr-requests";

    @LocalServerPort
    private int port;

    @Autowired
    TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    void verifyControllerReturns202AcceptedWhenCalled() {
        var expected = HttpStatus.ACCEPTED;

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add(EXTERNAL_REFERENCE_ID_PARAM_NAME, EXTERNAL_REFERENCE_ID);
        params.add(IMAGE_ENDPOINT_PARAM_NAME, IMAGE_ENDPOINT);
        params.add(EXTRACTED_TEXT_ENDPOINT_PARAM_NAME, EXTRACTED_TEXT_ENDPOINT);

        var uri = "http://localhost:" + port + APPLICATION_URL;

        var actual = this.restTemplate
                .postForEntity(uri, params, HttpStatus.class).getStatusCode();

        assertThat(actual, is(expected));
    }

    @Test
    void verifyControllerThrowsRestClientExceptionWhenRequiredParameterMissing() {

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add(EXTERNAL_REFERENCE_ID_PARAM_NAME, EXTERNAL_REFERENCE_ID);
        params.add(IMAGE_ENDPOINT_PARAM_NAME, IMAGE_ENDPOINT);
        // no extracted text endpoint parameter which is a required parameter

        var uri = "http://localhost:" + port + APPLICATION_URL;
        assertThrows(RestClientException.class, () -> this.restTemplate
                .postForEntity(uri, params, HttpStatus.class));
    }

}
