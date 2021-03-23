package uk.gov.companieshouse.ocrapiconsumer.kafka;

import static uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication.APPLICATION_NAME_SPACE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocrapiconsumer.kafka.exception.MaximumRetriesException;
import uk.gov.companieshouse.ocrapiconsumer.request.CallbackExtractedTextRestClient;

/*
   This Spring bean handles the final errors when a request can not be handled.
   This acts both as an extension point on acting on this exception and makes unit testing easier
*/
@Component
public class OcrMessageErrorHandler {
    private static final Logger LOG = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private final CallbackExtractedTextRestClient callbackExtractedTextRestClient;

    @Autowired
    public OcrMessageErrorHandler(CallbackExtractedTextRestClient callbackExtractedTextRestClient) {
        this.callbackExtractedTextRestClient = callbackExtractedTextRestClient;
    }

    public void handleMaximumRetriesException(String contextId, String responseId, MaximumRetriesException mre, String extractedTextEndpoint) {
        LOG.errorContext(contextId, "Maximum Retries reached", mre, null);
        callbackExtractedTextRestClient.sendTextResultError(contextId, responseId, extractedTextEndpoint);
    }

    public void generalExceptionAfterRetry(String contextId, String responseId, Exception mre, String extractedTextEndpoint) {
        LOG.errorContext(contextId, "Unexpected Error when retrying message", mre, null);
        callbackExtractedTextRestClient.sendTextResultError(contextId, responseId, extractedTextEndpoint);
    }

	public void generalException(String contextId, String responseId, Exception exception, String extractedTextEndpoint) {
        LOG.errorContext(contextId, "Unexpected Error ", exception, null);
        callbackExtractedTextRestClient.sendTextResultError(contextId, responseId, extractedTextEndpoint);
    }
    
}
