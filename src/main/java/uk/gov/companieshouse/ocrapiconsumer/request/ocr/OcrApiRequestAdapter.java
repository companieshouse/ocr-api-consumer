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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication;
import uk.gov.companieshouse.ocrapiconsumer.request.extractedtext.ExtractTextResultDTO;

@Component
public class OcrApiRequestAdapter {

    private static final String FILE_REQUEST_PARAMETER_NAME = "file";
    private static final String RESPONSE_ID_REQUEST_PARAMETER_NAME = "responseId";
    private static final Logger LOG = LoggerFactory.getLogger(OcrApiConsumerApplication.APPLICATION_NAME_SPACE);

    // Get the ocr api url from env variables
    private final EnvironmentReader environmentReader = new EnvironmentReaderImpl();
    private final String ocrApiUrl = environmentReader.getMandatoryUrl("OCR_API_URL");

    private final RestTemplate restTemplate;

    @Autowired
    public OcrApiRequestAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Sends the OCR request to the ocr-api
     * @param   externalReferenceID   The request ID.
     * @param   tiffContent           The image content retrieved from CHIPS.
     * @return  A response entity containing the extracted text result DTO.
     * @throws  HttpClientErrorException  When the request returns a 404 NOT FOUND.
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

        ResponseEntity<ExtractTextResultDTO> response;

        LOG.debug(ocrApiUrl);
        try {
            response = restTemplate.postForEntity(ocrApiUrl, entity, ExtractTextResultDTO.class);
        } catch(HttpClientErrorException | HttpServerErrorException exception) {
            LOG.error(exception);
            throw exception;
        }
        return response;
    }

}
