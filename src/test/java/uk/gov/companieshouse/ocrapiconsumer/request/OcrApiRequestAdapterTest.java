package uk.gov.companieshouse.ocrapiconsumer.request;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.ocrapiconsumer.common.EnvironmentVariable;
import uk.gov.companieshouse.ocrapiconsumer.groups.Unit;
import uk.gov.companieshouse.ocrapiconsumer.kafka.exception.RetryableErrorException;

@Unit
@ExtendWith(MockitoExtension.class)
class OcrApiRequestAdapterTest extends TestParent {

    private static final String DUMMY_OCR_API_URL = "https://dummyurl.com/ocr";
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private EnvironmentReader environmentReader;

    @InjectMocks
    private OcrApiRequestAdapter ocrApiRequestAdapter;

    @BeforeEach
    void setupTests() {
        ExtractTextResultDTO extractTextResultDTO = createMockTextResult();
        response = new ResponseEntity<>(extractTextResultDTO, HttpStatus.CREATED);
    }

    @Test
    void testSendOcrRequestSuccessful() {
        // given
        ResponseEntity<ExtractTextResultDTO> expected = response;
        when(environmentReader.getMandatoryUrl(EnvironmentVariable.OCR_API_URL.name())).thenReturn(DUMMY_OCR_API_URL);
        when(restTemplate.postForEntity(eq(DUMMY_OCR_API_URL), any(), eq(ExtractTextResultDTO.class))).thenReturn(response);

        // when
        ResponseEntity<ExtractTextResultDTO> actual = ocrApiRequestAdapter
                .sendOcrRequestToOcrApi(CONTEXT_ID, MOCK_TIFF_CONTENT, RESPONSE_ID);

        // then
        assertThat(actual, is(expected));
    }

    @Test
    @DisplayName("A rest client exception should be caught and a retryable exception thrown")
    void sendOcrRequestThrowsRetryableException() {
        when(restTemplate.postForEntity(eq(DUMMY_OCR_API_URL), any(), eq(ExtractTextResultDTO.class)))
                .thenThrow(RestClientException.class);

        assertThrows(RetryableErrorException.class, () ->
                ocrApiRequestAdapter.sendOcrRequestToOcrApi(CONTEXT_ID, MOCK_TIFF_CONTENT, RESPONSE_ID));
    }
}
