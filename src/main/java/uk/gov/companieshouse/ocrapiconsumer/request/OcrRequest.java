package uk.gov.companieshouse.ocrapiconsumer.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.commons.lang.StringUtils;

import uk.gov.companieshouse.ocr.OcrRequestMessage;

import javax.validation.constraints.NotBlank;

public class OcrRequest {

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

    @JsonProperty("context_id")
    private String contextId;

    public OcrRequest() {
        // added for jackson deserialising
    }

    public OcrRequest(String contextId, String imageEndpoint, String convertedTextEndpoint, String responseId) {

        this.contextId = contextId;
        this.imageEndpoint = imageEndpoint;
        this.convertedTextEndpoint = convertedTextEndpoint;
        this.responseId = responseId;
    }

    public OcrRequest(String imageEndpoint, String convertedTextEndpoint, String responseId) {
        this(responseId, imageEndpoint, convertedTextEndpoint, responseId);
    }

    public OcrRequest(OcrRequestMessage ocrRequestMessage) {
        this(ocrRequestMessage.getContextId(),
             ocrRequestMessage.getImageEndpoint(), 
             ocrRequestMessage.getConvertedTextEndpoint(), 
             ocrRequestMessage.getResponseId());
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

    public String getContextId() {
        if (StringUtils.isNotBlank(contextId)) {
          return contextId;
        }
        else {
            return responseId;
        }
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    
}
