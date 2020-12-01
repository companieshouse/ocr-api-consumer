package uk.gov.companieshouse.ocrapiconsumer.request.extractedtext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication;

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
     */
    public void sendTextResult(String extractedTextEndpoint, ExtractTextResultDTO extractedText) {
        HttpEntity<ExtractTextResultDTO> entity = new HttpEntity<>(extractedText);
        try {
            restTemplate.postForEntity(extractedTextEndpoint, entity, String.class);
        } catch(HttpClientErrorException | HttpServerErrorException exception) {
            LOG.error(exception);
            throw exception;
        }
    }
}
