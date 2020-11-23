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

    public void sendTextResult(String convertedTextEndpoint, ExtractTextResultDTO extractedText) {
        HttpEntity<ExtractTextResultDTO> entity = new HttpEntity<>(extractedText);
        restTemplate.postForEntity(convertedTextEndpoint, entity, String.class);
    }
}
