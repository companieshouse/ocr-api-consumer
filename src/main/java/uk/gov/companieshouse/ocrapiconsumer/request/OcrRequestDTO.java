package uk.gov.companieshouse.ocrapiconsumer.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;

public class OcrRequestDTO {

    // Use the NotBlank annotation as each field is required and cannot be blank or null
    @NotBlank(message = "Missing required value image_endpoint")
    @JsonProperty("image_endpoint")
    private String imageEndpoint;

    @NotBlank(message = "Missing required value converted_text_endpoint")
    @JsonProperty("converted_text_endpoint")
    private String convertedTextEndpoint;

    @NotBlank(message = "Missing required value response_id")
    @JsonProperty("response_id")
    private String responseId;

    public OcrRequestDTO(String imageEndpoint, String convertedTextEndpoint, String responseId) {
        this.imageEndpoint = imageEndpoint;
        this.convertedTextEndpoint = convertedTextEndpoint;
        this.responseId = responseId;
    }

    public String getImageEndpoint() {
        return imageEndpoint;
    }

    public void setImageEndpoint(String imageEndpoint) {
        this.imageEndpoint = imageEndpoint;
    }

    public String getConvertedTextEndpoint() {
        return convertedTextEndpoint;
    }

    public void setConvertedTextEndpoint(String convertedTextEndpoint) {
        this.convertedTextEndpoint = convertedTextEndpoint;
    }

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }
}
