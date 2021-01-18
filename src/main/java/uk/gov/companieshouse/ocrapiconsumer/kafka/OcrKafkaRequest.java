package uk.gov.companieshouse.ocrapiconsumer.kafka;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import uk.gov.companieshouse.ocrapiconsumer.request.OcrRequest;

public class OcrKafkaRequest extends OcrRequest {

    public OcrKafkaRequest() {

    }

    public OcrKafkaRequest(String imageEndpoint, String convertedTextEndpoint, String responseId) {
        super(imageEndpoint, convertedTextEndpoint, responseId);
    }

    public OcrKafkaRequest(
        String imageEndpoint, String convertedTextEndpoint, String responseId, String applicationId, Date createdAt, Integer attempt
    ) {
        super(imageEndpoint, convertedTextEndpoint, responseId);

        this.applicationId = applicationId;
        this.createdAt = createdAt;
        this.attempt = attempt;
    }

    @JsonProperty("app_id")
    private String applicationId;

    @JsonProperty("created_at")
    private Date createdAt;

    @JsonProperty("attempt")
    private Integer attempt;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getAttempt() {
        return attempt;
    }

    public void setAttempt(Integer attempt) {
        this.attempt = attempt;
    }
}
