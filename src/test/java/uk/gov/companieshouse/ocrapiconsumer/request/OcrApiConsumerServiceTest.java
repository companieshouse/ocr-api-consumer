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
        extractTextResultDTO = createMockTextResult();
        response = new ResponseEntity<>(extractTextResultDTO, HttpStatus.CREATED);
    }

    @Test
    void testOcrApiServiceLogsSuccessfully() {
        // given
        when(chipsImageAdapter.getTiffImageFromChips(IMAGE_ENDPOINT)).thenReturn(MOCK_TIFF_CONTENT);
        when(ocrApiRequestAdapter.sendOcrRequestToOcrApi(MOCK_TIFF_CONTENT, RESPONSE_ID)).thenReturn(response);

        // when
        ocrApiConsumerService.logOcrRequest(IMAGE_ENDPOINT, CONVERTED_TEXT_ENDPOINT, RESPONSE_ID);

        // then
        verify(chipsImageAdapter).getTiffImageFromChips(IMAGE_ENDPOINT);
        verify(ocrApiRequestAdapter).sendOcrRequestToOcrApi(MOCK_TIFF_CONTENT, RESPONSE_ID);
        verify(chipsExtractedTextAdapter).sendTextResult(CONVERTED_TEXT_ENDPOINT, extractTextResultDTO);
    }
}
