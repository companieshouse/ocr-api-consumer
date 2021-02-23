package uk.gov.companieshouse.ocrapiconsumer.kafka.exception;

/**
 * Call on potential coding bug or Kafka serious problem (e.g. can not sent message to retry topic) so we want
 * to let Caller know that the ocr-request can not be made
 */
public class FatalErrorException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public FatalErrorException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
