package uk.gov.companieshouse.ocrapiconsumer.request.extractedtext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ChipsExtractedTextAdapter {

    private final RestTemplate restTemplate;

    @Autowired
    public ChipsExtractedTextAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendTextResult(String convertedTextEndpoint, ExtractTextResultDTO extractedText)
            throws ExtractedTextEndpointNotFoundException {
        HttpEntity<ExtractTextResultDTO> entity = new HttpEntity<>(extractedText);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(convertedTextEndpoint, entity, String.class);

        if(responseEntity.getStatusCode().equals(HttpStatus.NOT_FOUND))
            throw new ExtractedTextEndpointNotFoundException();
    }
}
