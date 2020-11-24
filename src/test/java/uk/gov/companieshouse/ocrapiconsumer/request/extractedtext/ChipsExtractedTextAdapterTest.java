package uk.gov.companieshouse.ocrapiconsumer.request.extractedtext;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
import uk.gov.companieshouse.ocrapiconsumer.request.TestParent;

@Unit
@ExtendWith(MockitoExtension.class)
class ChipsExtractedTextAdapterTest extends TestParent {

    private ExtractTextResultDTO extractTextResultDTO;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ChipsExtractedTextAdapter chipsExtractedTextAdapter;

    @BeforeEach
    void setupTests() {
        extractTextResultDTO = new ExtractTextResultDTO();
        extractTextResultDTO.setResponseId(EXTERNAL_REFERENCE_ID);
        extractTextResultDTO.setLowestConfidenceScore(LOWEST_CONFIDENCE_SCORE);
        extractTextResultDTO.setAverageConfidenceScore(AVERAGE_CONFIDENCE_SCORE);
        extractTextResultDTO.setExtractedText(EXTRACTED_TEXT);
        extractTextResultDTO.setOcrProcessingTimeMs(OCR_PROCESSING_TIME);
        extractTextResultDTO.setTotalProcessingTimeMs(TOTAL_PROCESSING_TIME);
    }

    @Test
    void testSendExtractedTextSuccessfully() throws ExtractedTextEndpointNotFoundException {
        // given
        when(restTemplate.postForEntity(eq(EXTRACTED_TEXT_ENDPOINT), any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // when
        chipsExtractedTextAdapter.sendTextResult(EXTRACTED_TEXT_ENDPOINT, extractTextResultDTO);

        // then
        verify(restTemplate).postForEntity(eq(EXTRACTED_TEXT_ENDPOINT), any(), any());
    }

    @Test
    void testSendExtractedTextNotFoundException() {
        // given
        when(restTemplate.postForEntity(eq(EXTRACTED_TEXT_ENDPOINT), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        // then
        assertThrows(ExtractedTextEndpointNotFoundException.class, () ->
                chipsExtractedTextAdapter.sendTextResult(EXTRACTED_TEXT_ENDPOINT, extractTextResultDTO));

    }
}
