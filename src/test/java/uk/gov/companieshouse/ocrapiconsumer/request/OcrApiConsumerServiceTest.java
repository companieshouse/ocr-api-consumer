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
    private OcrApiRequestRestClient ocrApiRequestRestClient;

    @Mock
    private ImageRestClient imageRestClient;

    @Mock
    private CallbackExtractedTextRestClient callbackExtractedTextRestClient;

    @InjectMocks
    private OcrApiConsumerService ocrApiConsumerService;

    @BeforeEach
    void setupTests() {
        extractTextResultDTO = createMockTextResult();
        response = new ResponseEntity<>(extractTextResultDTO, HttpStatus.CREATED);
    }

    @Test
    void testOcrApiServiceLogsSuccessfully() {

        OcrRequestDTO testOcrRequestDTO = new OcrRequestDTO(IMAGE_ENDPOINT, EXTRACTED_TEXT_ENDPOINT, RESPONSE_ID);

        // given
        when(imageRestClient.getImageContentsFromEndpoint(testOcrRequestDTO.getContextId(), testOcrRequestDTO.getImageEndpoint())).thenReturn(MOCK_TIFF_CONTENT);
        when(ocrApiRequestRestClient.obtainExtractTextResult(testOcrRequestDTO.getContextId(), MOCK_TIFF_CONTENT, testOcrRequestDTO.getResponseId())).thenReturn(response);

        // when
        ocrApiConsumerService.processOcrRequest(testOcrRequestDTO);

        // then
        verify(callbackExtractedTextRestClient).sendTextResult(testOcrRequestDTO.getConvertedTextEndpoint(), extractTextResultDTO);
    }
}
