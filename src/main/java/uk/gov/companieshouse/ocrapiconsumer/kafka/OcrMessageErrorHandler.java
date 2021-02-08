package uk.gov.companieshouse.ocrapiconsumer.kafka;

import static uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication.APPLICATION_NAME_SPACE;

import org.springframework.stereotype.Component;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocrapiconsumer.kafka.exception.MaximumRetriesException;

/*
   This Spring bean handles the final errors when a request can not be handled. This acts both as an extension point on acting on this execption and makes unit testing easier
*/
@Component
public class OcrMessageErrorHandler {

    private static final Logger LOG = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);


    public void handleMaximumRetriesException(String contextId, MaximumRetriesException mre) {
        LOG.errorContext(contextId, "Maximum Retries reached", mre, null);

    }

    public void generalExceptionAfterRetry(String contextId, Exception mre) {
        LOG.errorContext(contextId, "Unexpected Error when retrying message", mre, null);

    }

	public void generalException(String contextId, Exception exception) {
        LOG.errorContext(contextId, "Unexpected Error ", exception, null);

	}
    
}
