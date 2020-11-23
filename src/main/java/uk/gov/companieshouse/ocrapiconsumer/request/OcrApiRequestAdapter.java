package uk.gov.companieshouse.ocrapiconsumer.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication;
import uk.gov.companieshouse.ocrapiconsumer.common.MultipartTiff;

@Component
public class OcrApiRequestAdapter {

    private static final String OCR_API_ENDPOINT = "http://localhost:9090/dummy-endpoint";
    private static final String RESPONSE_ID_REQUEST_PARAMETER_NAME = "responseId";
    private static final String FILE_REQUEST_PARAMETER_NAME = "file";

    private static final Logger LOG = LoggerFactory.getLogger(OcrApiConsumerApplication.APPLICATION_NAME_SPACE);

    private final RestTemplate restTemplate;

    @Autowired
    public OcrApiRequestAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<ExtractTextResultDTO> sendOcrRequestToOcrApi(String externalReferenceID, byte[] tiffContent) {

        MultipartTiff multipartTiff = convertByteArrayToMultipartTiff(externalReferenceID, tiffContent);

        LOG.debugContext(externalReferenceID,
                String.format("Building URI with URL: %s, Query Params: %s, %s",
                        OCR_API_ENDPOINT, RESPONSE_ID_REQUEST_PARAMETER_NAME, FILE_REQUEST_PARAMETER_NAME),
                null);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(OCR_API_ENDPOINT)
                .queryParam(RESPONSE_ID_REQUEST_PARAMETER_NAME, externalReferenceID)
                .queryParam(FILE_REQUEST_PARAMETER_NAME, multipartTiff);

        HttpEntity<String> entity = new HttpEntity<>("");
        return restTemplate.postForEntity(builder.toUriString(), entity, ExtractTextResultDTO.class);
    }

    private MultipartTiff convertByteArrayToMultipartTiff(String externalReferenceID, byte[] tiffContent) {
        LOG.debugContext(externalReferenceID, "Creating MultipartTiff from byte array image contents", null);
        return new MultipartTiff(tiffContent);
    }
}
