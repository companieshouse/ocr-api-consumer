package uk.gov.companieshouse.ocrapiconsumer.request.ocr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import uk.gov.companieshouse.ocrapiconsumer.request.extractedtext.ExtractTextResultDTO;

@Unit
@ExtendWith(MockitoExtension.class)
class OcrApiRequestAdapterTest extends TestParent {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OcrApiRequestAdapter ocrApiRequestAdapter;

    @BeforeEach
    void setupTests() {
        ExtractTextResultDTO extractTextResultDTO = new ExtractTextResultDTO();
        extractTextResultDTO.setResponseId(EXTERNAL_REFERENCE_ID);
        extractTextResultDTO.setLowestConfidenceScore(LOWEST_CONFIDENCE_SCORE);
        extractTextResultDTO.setAverageConfidenceScore(AVERAGE_CONFIDENCE_SCORE);
        extractTextResultDTO.setExtractedText(EXTRACTED_TEXT);
        extractTextResultDTO.setOcrProcessingTimeMs(OCR_PROCESSING_TIME);
        extractTextResultDTO.setTotalProcessingTimeMs(TOTAL_PROCESSING_TIME);
        response = new ResponseEntity<>(extractTextResultDTO, HttpStatus.CREATED);
    }

    @Test
    void testSendOcrRequestSuccessful() throws OcrServiceUnavailableException {
        // given
        ResponseEntity<ExtractTextResultDTO> expected = response;
        when(restTemplate.postForEntity(anyString(), any(), eq(ExtractTextResultDTO.class))).thenReturn(response);

        // when
        ResponseEntity<ExtractTextResultDTO> actual = ocrApiRequestAdapter
                .sendOcrRequestToOcrApi(EXTERNAL_REFERENCE_ID, MOCK_TIFF_CONTENT);

        // then
        assertThat(actual, is(expected));
    }

    @Test
    void testSendOcrRequestServiceUnavailableException() {
        // given
        when(restTemplate.postForEntity(anyString(), any(), eq(ExtractTextResultDTO.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        // then
        assertThrows(OcrServiceUnavailableException.class, () ->
                ocrApiRequestAdapter.sendOcrRequestToOcrApi(EXTERNAL_REFERENCE_ID, MOCK_TIFF_CONTENT));

    }
}
