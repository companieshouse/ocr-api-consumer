package uk.gov.companieshouse.ocrapiconsumer.kafka.exception;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.ocrapiconsumer.groups.Unit;
import uk.gov.companieshouse.ocrapiconsumer.kafka.OcrMessageErrorHandler;
import uk.gov.companieshouse.ocrapiconsumer.request.CallbackExtractedTextRestClient;
import uk.gov.companieshouse.ocrapiconsumer.request.TestParent;

@Unit
@ExtendWith(MockitoExtension.class)
class OcrMessageErrorHandlerTest extends TestParent {

    @Mock
    private CallbackExtractedTextRestClient callbackExtractedTextRestClient;

    @InjectMocks
    private OcrMessageErrorHandler ocrMessageErrorHandler;

    @Test
    @DisplayName("handleMaximumRetryException should call CallbackExtractedTextAdapter to send error response")
    void verifyHandleMaximumRetriesExceptionCallsSendTextResultError() {
        //given
        MaximumRetriesException mre = new MaximumRetriesException();

        // when
        ocrMessageErrorHandler.handleMaximumRetriesException(CONTEXT_ID, RESPONSE_ID,
                mre, EXTRACTED_TEXT_ENDPOINT);

        // then
        verify(callbackExtractedTextRestClient, times(1))
                .sendTextResultError(CONTEXT_ID, RESPONSE_ID, EXTRACTED_TEXT_ENDPOINT);
    }

    @Test
    @DisplayName("generalExecptionAfterRetry should call CallbackExtractedTextAdapter to send error response")
    void verifyGeneralExceptionAfterRetryCallsSendTextResultError() {
        //given
        Exception e = new Exception();

        // when
        ocrMessageErrorHandler.generalExceptionAfterRetry(CONTEXT_ID, RESPONSE_ID, e, EXTRACTED_TEXT_ENDPOINT);

        // then
        verify(callbackExtractedTextRestClient, times(1))
                .sendTextResultError(CONTEXT_ID, RESPONSE_ID, EXTRACTED_TEXT_ENDPOINT);
    }

    @Test
    @DisplayName("generalException should call CallbackExtractedTextAdapter to send error response")
    void verifyGeneralExceptionCallsSendTextResultError() {
        //given
        Exception e = new Exception();

        // when
        ocrMessageErrorHandler.generalException(CONTEXT_ID, RESPONSE_ID, e, EXTRACTED_TEXT_ENDPOINT);

        // then
        verify(callbackExtractedTextRestClient, times(1))
                .sendTextResultError(CONTEXT_ID, RESPONSE_ID, EXTRACTED_TEXT_ENDPOINT);
    }
}
