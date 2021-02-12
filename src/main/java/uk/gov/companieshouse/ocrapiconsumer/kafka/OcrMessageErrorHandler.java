package uk.gov.companieshouse.ocrapiconsumer.kafka;

import static uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication.APPLICATION_NAME_SPACE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocrapiconsumer.kafka.exception.MaximumRetriesException;
import uk.gov.companieshouse.ocrapiconsumer.request.CallbackExtractedTextAdapter;

/*
   This Spring bean handles the final errors when a request can not be handled.
   This acts both as an extension point on acting on this exception and makes unit testing easier
*/
@Component
public class OcrMessageErrorHandler {
    private static final Logger LOG = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private final CallbackExtractedTextAdapter callbackExtractedTextAdapter;

    @Autowired
    public OcrMessageErrorHandler(CallbackExtractedTextAdapter callbackExtractedTextAdapter) {
        this.callbackExtractedTextAdapter = callbackExtractedTextAdapter;
    }

    public void handleMaximumRetriesException(String contextId, MaximumRetriesException mre, String extractedTextEndpoint) {
        LOG.errorContext(contextId, "Maximum Retries reached", mre, null);
        callbackExtractedTextAdapter.sendTextResultError(contextId, extractedTextEndpoint);
    }

    public void generalExceptionAfterRetry(String contextId, Exception mre, String extractedTextEndpoint) {
        LOG.errorContext(contextId, "Unexpected Error when retrying message", mre, null);
        callbackExtractedTextAdapter.sendTextResultError(contextId, extractedTextEndpoint);
    }

	public void generalException(String contextId, Exception exception, String extractedTextEndpoint) {
        LOG.errorContext(contextId, "Unexpected Error ", exception, null);
        callbackExtractedTextAdapter.sendTextResultError(contextId, extractedTextEndpoint);
    }
    
}
