package uk.gov.companieshouse.ocrapiconsumer.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.ocrapiconsumer.kafka.exception.RetryableErrorException;

@Component
public class OcrApiRequestAdapter {

    private static final String FILE_REQUEST_PARAMETER_NAME = "file";
    private static final String RESPONSE_ID_REQUEST_PARAMETER_NAME = "responseId";
    private static final String CONTEXT_ID_REQUEST_PARAMETER_NAME = "contextId";

    private final RestTemplate restTemplate;

    @Value("${ocr.api.url}")
    protected String ocrApiUrl;

    @Autowired
    public OcrApiRequestAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Sends the OCR request to the ocr-api
     * @param   contextId             Context Logging key between microservices
     * @param   tiffContent           The image content retrieved from CHIPS.
     * @param   responseId            The request ID.
     * @return  A response entity containing the extracted text result DTO.
     */
    public ResponseEntity<ExtractTextResultDTO> sendOcrRequestToOcrApi(String contextId, byte[] tiffContent, String responseId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Filename is required to send the byte array as a file to the MultipartFile parameter.
        String filename = responseId + ".tif";
        ByteArrayResource byteArrayResource = new ByteArrayResource(tiffContent) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add(FILE_REQUEST_PARAMETER_NAME, byteArrayResource);
        params.add(CONTEXT_ID_REQUEST_PARAMETER_NAME, contextId);
        params.add(RESPONSE_ID_REQUEST_PARAMETER_NAME, responseId);

        try {
            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(params, headers);
            
            return restTemplate.postForEntity(ocrApiUrl, entity, ExtractTextResultDTO.class);

        } catch (Exception e) {
             throw new RetryableErrorException("Fail calling ocr-api url [" + ocrApiUrl + "], error message [" + e.getMessage() + "]", e);
        }
    }
}
