package uk.gov.companieshouse.ocrapiconsumer.kafka.exception;

public class MaximumRetriesException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MaximumRetriesException() {
        super("Maximum retries have been attempted and message is still failing");
    }

}
