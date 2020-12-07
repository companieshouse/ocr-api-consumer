package uk.gov.companieshouse.ocrapiconsumer.request;

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
import uk.gov.companieshouse.ocrapiconsumer.groups.Unit;

@Unit
@ExtendWith(MockitoExtension.class)
class OcrApiConsumerServiceTest extends TestParent {

    @Mock
    private OcrApiRequestAdapter ocrApiRequestAdapter;

    @Mock
    private ChipsImageAdapter chipsImageAdapter;

    @Mock
    private ChipsExtractedTextAdapter chipsExtractedTextAdapter;

    @InjectMocks
    private OcrApiConsumerService ocrApiConsumerService;

    @BeforeEach
    void setupTests() {
        extractTextResultDTO = new ExtractTextResultDTO();
        extractTextResultDTO.setResponseId(EXTERNAL_REFERENCE_ID);
        extractTextResultDTO.setLowestConfidenceScore(LOWEST_CONFIDENCE_SCORE);
        extractTextResultDTO.setAverageConfidenceScore(AVERAGE_CONFIDENCE_SCORE);
        extractTextResultDTO.setExtractedText(EXTRACTED_TEXT);
        extractTextResultDTO.setOcrProcessingTimeMs(OCR_PROCESSING_TIME);
        extractTextResultDTO.setTotalProcessingTimeMs(TOTAL_PROCESSING_TIME);
        response = new ResponseEntity<>(extractTextResultDTO, HttpStatus.CREATED);
    }

    @Test
    void testOcrApiServiceLogsSuccessfully() {
        // given
        when(chipsImageAdapter.getTiffImageFromChips(IMAGE_ENDPOINT)).thenReturn(MOCK_TIFF_CONTENT);
        when(ocrApiRequestAdapter.sendOcrRequestToOcrApi(EXTERNAL_REFERENCE_ID, MOCK_TIFF_CONTENT)).thenReturn(response);

        // when
        ocrApiConsumerService.logOcrRequest(EXTERNAL_REFERENCE_ID, IMAGE_ENDPOINT, EXTRACTED_TEXT_ENDPOINT);

        // then
        verify(chipsImageAdapter).getTiffImageFromChips(IMAGE_ENDPOINT);
        verify(ocrApiRequestAdapter).sendOcrRequestToOcrApi(EXTERNAL_REFERENCE_ID, MOCK_TIFF_CONTENT);
        verify(chipsExtractedTextAdapter).sendTextResult(EXTRACTED_TEXT_ENDPOINT, extractTextResultDTO);
    }
}
