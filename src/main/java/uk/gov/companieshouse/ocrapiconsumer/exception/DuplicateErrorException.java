package uk.gov.companieshouse.ocrapiconsumer.exception;

public class DuplicateErrorException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateErrorException(String message) {
        super(message);
    }

}
