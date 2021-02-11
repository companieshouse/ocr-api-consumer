package uk.gov.companieshouse.ocrapiconsumer.kafka.exception;

import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication.APPLICATION_NAME_SPACE;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocrapiconsumer.groups.Unit;
import uk.gov.companieshouse.ocrapiconsumer.kafka.OcrMessageErrorHandler;
import uk.gov.companieshouse.ocrapiconsumer.request.CallbackExtractedTextAdapter;
import uk.gov.companieshouse.ocrapiconsumer.request.TestParent;

@Unit
@ExtendWith(MockitoExtension.class)
class OcrMessageErrorHandlerTest extends TestParent {

    @Mock
    private CallbackExtractedTextAdapter callbackExtractedTextAdapter;

    @InjectMocks
    private OcrMessageErrorHandler ocrMessageErrorHandler;

    @Test
    @DisplayName("handleMaximumRetryException should call CallbackExtractedTextAdapter to send error response")
    void verifyHandleMaximumRetriesExceptionCallsSendTextResultError() {
        //given
        MaximumRetriesException mre = new MaximumRetriesException();

        // when
        ocrMessageErrorHandler.handleMaximumRetriesException(RESPONSE_ID,
                mre, EXTRACTED_TEXT_ENDPOINT);

        // then
        verify(callbackExtractedTextAdapter, times(1)).sendTextResultError(RESPONSE_ID, EXTRACTED_TEXT_ENDPOINT);
    }
}
