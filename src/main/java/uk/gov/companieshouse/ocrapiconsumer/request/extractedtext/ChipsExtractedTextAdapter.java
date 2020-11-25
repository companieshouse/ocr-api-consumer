package uk.gov.companieshouse.ocrapiconsumer.request.extractedtext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication;

import java.io.IOException;

@Component
public class ChipsExtractedTextAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(OcrApiConsumerApplication.APPLICATION_NAME_SPACE);

    private final RestTemplate restTemplate;

    @Autowired
    public ChipsExtractedTextAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Sends the extracted text to CHIPS for use in rules.
     * @param   extractedTextEndpoint   The endpoint to send the extracted text to.
     * @param   extractedText           The extracted text DTO object.
     * @throws  IOException             When the extracted text endpoint response returns a 404 NOT FOUND.
     */
    public void sendTextResult(String extractedTextEndpoint, ExtractTextResultDTO extractedText)
            throws IOException {
        HttpEntity<ExtractTextResultDTO> entity = new HttpEntity<>(extractedText);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(extractedTextEndpoint, entity, String.class);

        if(responseEntity.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            IOException exception = new IOException();
            LOG.error(exception);
            throw exception;
        }
    }
}
