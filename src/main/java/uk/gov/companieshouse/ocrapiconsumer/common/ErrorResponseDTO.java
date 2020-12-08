package uk.gov.companieshouse.ocrapiconsumer.common;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *  The response to the client when an error occurs.
 */
public class ErrorResponseDTO {

    @JsonProperty("error_message")
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}