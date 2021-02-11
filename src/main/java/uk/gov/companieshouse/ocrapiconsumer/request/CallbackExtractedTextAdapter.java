package uk.gov.companieshouse.ocrapiconsumer.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import uk.gov.companieshouse.ocrapiconsumer.kafka.exception.RetryableErrorException;

@Component
public class CallbackExtractedTextAdapter {

    public static final String OCR_CONVERSION_ERROR_TEXT = "UNABLE_TO_PROCESS_OCR_CONVERSION";
    private final RestTemplate restTemplate;

    @Autowired
    public CallbackExtractedTextAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Sends the extracted text to CHIPS for use in rules.
     * @param   extractedTextEndpoint   The endpoint to send the extracted text to.
     * @param   extractedText           The extracted text DTO object.
     */
    public void sendTextResult(String extractedTextEndpoint, ExtractTextResultDTO extractedText) {
        
        try {
            HttpEntity<ExtractTextResultDTO> entity = new HttpEntity<>(extractedText);
            restTemplate.postForEntity(extractedTextEndpoint, entity, String.class);

        } catch (Exception e) {
             throw new RetryableErrorException("Fail to get OCR Text converted by ocr-api [" + e.getMessage() + "]", e);
        }
    }

    /**
     * Sends the extracted text with default values to CHIPS for non-retryable errors
     * @param   contextId               The context ID of the application.
     * @param   extractedTextEndpoint   The endpoint to send the extracted text to.
     */
    public void sendTextResultError(String contextId, String extractedTextEndpoint) {
        ExtractTextResultDTO extractedTextError = createErrorExtractTextResultDTO(contextId);

        HttpEntity<ExtractTextResultDTO> entity = new HttpEntity<>(extractedTextError);
        restTemplate.postForEntity(extractedTextEndpoint, entity, String.class);
    }

    /**
     * Creates an extracted text result with default values, for use in non-retryable errors.
     * @param contextId The context ID of the application.
     * @return          An ExtractTextResultDTO object with default values.
     */
    private ExtractTextResultDTO createErrorExtractTextResultDTO(String contextId) {
        ExtractTextResultDTO extractedTextError = new ExtractTextResultDTO();
        extractedTextError.setAverageConfidenceScore(0);
        extractedTextError.setLowestConfidenceScore(0);
        extractedTextError.setOcrProcessingTimeMs(0);
        extractedTextError.setTotalProcessingTimeMs(0);
        extractedTextError.setResponseId(contextId);
        extractedTextError.setExtractedText(OCR_CONVERSION_ERROR_TEXT);
        return extractedTextError;
    }
}
