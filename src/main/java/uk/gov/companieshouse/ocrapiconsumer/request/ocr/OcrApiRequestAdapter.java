package uk.gov.companieshouse.ocrapiconsumer.request.ocr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;
import uk.gov.companieshouse.ocrapiconsumer.request.extractedtext.ExtractTextResultDTO;

@Component
public class OcrApiRequestAdapter {

    private static final String FILE_REQUEST_PARAMETER_NAME = "file";
    private static final String RESPONSE_ID_REQUEST_PARAMETER_NAME = "responseId";

    private final String ocrApiUrl;
    private final RestTemplate restTemplate;

    @Autowired
    public OcrApiRequestAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.ocrApiUrl = readOcrApiUrlFromEnv();
    }

    /**
     * Sends the OCR request to the ocr-api
     * @param   externalReferenceID   The request ID.
     * @param   tiffContent           The image content retrieved from CHIPS.
     * @return  A response entity containing the extracted text result DTO.
     */
    public ResponseEntity<ExtractTextResultDTO> sendOcrRequestToOcrApi(String externalReferenceID, byte[] tiffContent) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Filename is required to send the byte array as a file to the MultipartFile parameter.
        String filename = externalReferenceID + ".tif";
        ByteArrayResource byteArrayResource = new ByteArrayResource(tiffContent) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add(FILE_REQUEST_PARAMETER_NAME, byteArrayResource);
        params.add(RESPONSE_ID_REQUEST_PARAMETER_NAME, externalReferenceID);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(params, headers);

        return restTemplate.postForEntity(ocrApiUrl, entity, ExtractTextResultDTO.class);
    }

    /**
     * Reads in the OCR API URL from environment variables using the Environment Reader.
     * @return  The OCR API URL as a string
     */
    private String readOcrApiUrlFromEnv() {
        // Get the ocr api url from env variables
        final EnvironmentReader environmentReader = new EnvironmentReaderImpl();
        return environmentReader.getMandatoryUrl("OCR_API_URL");
    }

}
