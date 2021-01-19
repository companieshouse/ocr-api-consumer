package uk.gov.companieshouse.ocrapiconsumer.exception;

public class DuplicateErrorException extends RuntimeException {

    public DuplicateErrorException(String message) {
        super(message);
    }

}
