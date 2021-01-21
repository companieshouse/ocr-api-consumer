package uk.gov.companieshouse.ocrapiconsumer.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import uk.gov.companieshouse.ocrapiconsumer.exception.RetryableErrorException;

@Component
public class ExtractedTextCallbackAdapter {

    private final RestTemplate restTemplate;

    @Autowired
    public ExtractedTextCallbackAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Sends the extracted text to Requester.
     * @param   extractedTextEndpoint   The endpoint to send the extracted text to.
     * @param   extractedText           The extracted text DTO object.
     */
    public void sendTextResult(String extractedTextEndpoint, ExtractTextResultDTO extractedText) {

        try {
            HttpEntity<ExtractTextResultDTO> entity = new HttpEntity<>(extractedText);
            restTemplate.postForEntity(extractedTextEndpoint, entity, String.class);

        } catch (Exception e) {
             throw new RetryableErrorException("Fail to get OCR Text converted by ocr-api [" + e.getMessage() + "]");
        }
    }
}
