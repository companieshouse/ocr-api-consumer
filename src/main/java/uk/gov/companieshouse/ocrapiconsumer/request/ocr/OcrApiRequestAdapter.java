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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication;
import uk.gov.companieshouse.ocrapiconsumer.common.MultipartTiff;
import uk.gov.companieshouse.ocrapiconsumer.request.extractedtext.ExtractTextResultDTO;

import java.io.IOException;

@Component
public class OcrApiRequestAdapter {

    private static final String OCR_API_ENDPOINT = "http://localhost:9090/ocr";
    private static final String FILE_REQUEST_PARAMETER_NAME = "file";
    private static final String RESPONSE_ID_REQUEST_PARAMETER_NAME = "responseId";

    private static final Logger LOG = LoggerFactory.getLogger(OcrApiConsumerApplication.APPLICATION_NAME_SPACE);

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
    public ResponseEntity<ExtractTextResultDTO> sendOcrRequestToOcrApi(String externalReferenceID, byte[] tiffContent) throws IOException {

        MultipartFile multipartFile = convertByteArrayToMultipartTiff(externalReferenceID, tiffContent);

        LOG.debugContext(externalReferenceID,
                String.format("Building URI with URL: %s, Query Params: %s, %s",
                        OCR_API_ENDPOINT, FILE_REQUEST_PARAMETER_NAME, RESPONSE_ID_REQUEST_PARAMETER_NAME),
                null);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add(FILE_REQUEST_PARAMETER_NAME, new ByteArrayResource(multipartFile.getBytes()));
        params.add(RESPONSE_ID_REQUEST_PARAMETER_NAME, externalReferenceID);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(params, headers);

        ResponseEntity<ExtractTextResultDTO> response;
        try {
            response = restTemplate.postForEntity(OCR_API_ENDPOINT, entity, ExtractTextResultDTO.class);
        } catch(HttpClientErrorException exception) {
            LOG.error(exception);
            throw exception;
        }

        return response;
    }

    private MultipartFile convertByteArrayToMultipartTiff(String externalReferenceID, byte[] tiffContent) {
        LOG.debugContext(externalReferenceID, "Creating MultipartTiff from byte array image contents", null);
        return new MultipartTiff(tiffContent);
    }
}
