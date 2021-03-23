package uk.gov.companieshouse.ocrapiconsumer.request;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.ocrapiconsumer.groups.Unit;
import uk.gov.companieshouse.ocrapiconsumer.kafka.exception.RetryableErrorException;

@Unit
@ExtendWith(MockitoExtension.class)
class CallbackExtractedTextRestClientTest extends TestParent {

    private ExtractTextResultDTO extractTextResultDTO;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CallbackExtractedTextRestClient callbackExtractedTextRestClient;

    @BeforeEach
    void setupTests() {
        extractTextResultDTO = createMockTextResult();
    }

    @Test
    void testSendExtractedTextSuccessfully() {
        // given
        when(restTemplate.postForEntity(eq(EXTRACTED_TEXT_ENDPOINT), any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // when
        callbackExtractedTextRestClient.sendTextResult(EXTRACTED_TEXT_ENDPOINT, extractTextResultDTO);

        // then
        verify(restTemplate, times(1)).postForEntity(eq(EXTRACTED_TEXT_ENDPOINT), any(), any());
    }

    @Test
    @DisplayName("A rest client exception should be caught and a retryable error exception thrown")
    void testSendExtractedTextUnsuccessful() {
        // given
        when(restTemplate.postForEntity(eq(EXTRACTED_TEXT_ENDPOINT), any(), any()))
                .thenThrow(RestClientException.class);

        assertThrows(RetryableErrorException.class, () ->
            callbackExtractedTextRestClient.sendTextResult(EXTRACTED_TEXT_ENDPOINT, extractTextResultDTO));
    }

    @Test
    void testSendExtractedTextError() {
        // given
        when(restTemplate.postForEntity(eq(EXTRACTED_TEXT_ENDPOINT), any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // when
        callbackExtractedTextRestClient.sendTextResultError(CONTEXT_ID, RESPONSE_ID, EXTRACTED_TEXT_ENDPOINT);

        // then
        verify(restTemplate, times(1)).postForEntity(eq(EXTRACTED_TEXT_ENDPOINT), any(), any());
    }


}
