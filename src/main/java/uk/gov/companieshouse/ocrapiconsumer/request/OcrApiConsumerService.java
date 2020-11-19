package uk.gov.companieshouse.ocrapiconsumer.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication;
import uk.gov.companieshouse.ocrapiconsumer.common.MultipartTiff;

@Service
public class OcrApiConsumerService {

    private static final String OCR_MICROSERVICE_ENDPOINT = "http://localhost:9090/dummy-endpoint";
    private static final String RESPONSE_ID_REQUEST_PARAMETER_NAME = "responseId";
    private static final String FILE_REQUEST_PARAMETER_NAME = "file";
    private static final Logger LOG = LoggerFactory.getLogger(OcrApiConsumerApplication.APPLICATION_NAME_SPACE);

    private final RestTemplate restTemplate;

    @Autowired
    public OcrApiConsumerService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Async
    public void logOcrRequest(String externalReferenceID, String imageEndpoint, String convertedTextEndpoint) {
        LOG.infoContext(externalReferenceID,
                String.format("Request received with ID: %s, Image Endpoint: %s, Converted Text Endpoint: %s",
                        externalReferenceID, imageEndpoint, convertedTextEndpoint),
                null);


        LOG.debugContext(externalReferenceID, "Getting the TIFF image", null);
        byte[] image = getTiffImage(imageEndpoint);

        LOG.debugContext(externalReferenceID, "Sending image to ocr microservice for conversion", null);
        ExtractTextResultDTO extractedText = sendRequestToOcrMicroservice(externalReferenceID, image).getBody();

        LOG.debugContext(externalReferenceID, "Sending the converted text response for the articles of association", null);
        sendConvertedText(convertedTextEndpoint, extractedText);
    }

    private byte[] getTiffImage(String imageEndpoint) {
        return restTemplate.getForEntity(imageEndpoint, byte[].class).getBody();
    }

    private ResponseEntity<ExtractTextResultDTO> sendRequestToOcrMicroservice(String externalReferenceID, byte[] image) {

        MultipartTiff multipartTiff = convertByteArrayToMultipartTiff(image);

        LOG.debug("Building URI with URL: " + OCR_MICROSERVICE_ENDPOINT
                + ", Query Params: " + RESPONSE_ID_REQUEST_PARAMETER_NAME + ", " + FILE_REQUEST_PARAMETER_NAME);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(OCR_MICROSERVICE_ENDPOINT)
                .queryParam(RESPONSE_ID_REQUEST_PARAMETER_NAME, externalReferenceID)
                .queryParam(FILE_REQUEST_PARAMETER_NAME, multipartTiff);

        HttpEntity<String> entity = new HttpEntity<>("");
        return restTemplate.postForEntity(builder.toUriString(), entity, ExtractTextResultDTO.class);
    }

    private void sendConvertedText(String convertedTextEndpoint, ExtractTextResultDTO extractedText) {
        HttpEntity<ExtractTextResultDTO> entity = new HttpEntity<>(extractedText);
        restTemplate.postForEntity(convertedTextEndpoint, entity, String.class);
    }

    private MultipartTiff convertByteArrayToMultipartTiff(byte[] tiffContent) {
        LOG.debug("Creating MultipartTiff from byte array image contents");
        return new MultipartTiff(tiffContent);
    }
}
