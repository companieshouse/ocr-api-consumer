package uk.gov.companieshouse.ocrapiconsumer.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ChipsExtractedTextAdapter {

    private final RestTemplate restTemplate;

    @Autowired
    public ChipsExtractedTextAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Sends the extracted text to CHIPS for use in rules.
     * @param   extractedTextEndpoint   The endpoint to send the extracted text to.
     * @param   extractedText           The extracted text DTO object.
     */
    public void sendTextResult(String extractedTextEndpoint, ExtractTextResultDTO extractedText) {
        HttpEntity<ExtractTextResultDTO> entity = new HttpEntity<>(extractedText);
        restTemplate.postForEntity(extractedTextEndpoint, entity, String.class);
    }
}
