package uk.gov.companieshouse.ocrapiconsumer.kafka.exception;

public class FatalErrorException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public FatalErrorException(String message) {
        super(message);
    }

}
