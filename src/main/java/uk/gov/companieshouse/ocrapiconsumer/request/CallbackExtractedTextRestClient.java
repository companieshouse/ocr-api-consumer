package uk.gov.companieshouse.ocrapiconsumer.request;

import static uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication.APPLICATION_NAME_SPACE;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocrapiconsumer.kafka.exception.RetryableErrorException;

@Component
public class CallbackExtractedTextRestClient {

    private static final Logger LOG = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private final RestTemplate restTemplate;

    @Autowired
    public CallbackExtractedTextRestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Sends the extracted text to the extracted text endpoint.
     * @param   extractedTextEndpoint   The endpoint to send the extracted text to.
     * @param   extractedText           The extracted text DTO object.
     */
    public void sendTextResult(String extractedTextEndpoint, ExtractTextResultDTO extractedText) {
        
        try {
            HttpEntity<ExtractTextResultDTO> entity = new HttpEntity<>(extractedText);
            restTemplate.postForEntity(extractedTextEndpoint, entity, String.class);

        } catch (Exception e) {
             throw new RetryableErrorException("Fail to send results back to calling application at url [" + extractedTextEndpoint + "], error message [" + e.getMessage() + "]", e);
        }
    }

    /**
     * Sends the extracted text with default values to the extracted text endpoint for non-retryable errors
     * @param   contextId               The CHS context ID 
     * @param   responseId              The Response ID of the request
     * @param   extractedTextEndpoint   The endpoint to send the extracted text to.
     */
    public void sendTextResultError(String contextId, String responseId, String extractedTextEndpoint) {
        ExtractTextResultDTO extractedTextError = ExtractTextResultDTO
                .createErrorExtractTextResultDTOFromContextId(contextId, responseId);

        LOG.infoContext(contextId, "Sending Error Message back to client", extractedTextError.metadataMap());

        try {
            HttpEntity<ExtractTextResultDTO> entity = new HttpEntity<>(extractedTextError);
            restTemplate.postForEntity(extractedTextEndpoint, entity, String.class);
        } catch (Exception e) {
            // Log the exception instead of re-throwing as it could cause an infinite loop of RetryableErrorExceptions
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("extractedTextEndpoint", extractedTextEndpoint);
            LOG.errorContext(contextId, e, data);
        }
    }

}
