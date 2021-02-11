package uk.gov.companieshouse.ocrapiconsumer.request;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.ocrapiconsumer.groups.Unit;

@Unit
@ExtendWith(MockitoExtension.class)
class CallbackExtractedTextAdapterTest extends TestParent {

    private ExtractTextResultDTO extractTextResultDTO;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CallbackExtractedTextAdapter callbackExtractedTextAdapter;

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
        callbackExtractedTextAdapter.sendTextResult(EXTRACTED_TEXT_ENDPOINT, extractTextResultDTO);

        // then
        verify(restTemplate).postForEntity(eq(EXTRACTED_TEXT_ENDPOINT), any(), any());
    }

    @Test
    void testSendExtractedTextError() {
        // given
        when(restTemplate.postForEntity(eq(EXTRACTED_TEXT_ENDPOINT), any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // when
        callbackExtractedTextAdapter.sendTextResultError(EXTRACTED_TEXT_ENDPOINT, EXTRACTED_TEXT_ENDPOINT);

        // then
        verify(restTemplate).postForEntity(eq(EXTRACTED_TEXT_ENDPOINT), any(), any());
    }
}
