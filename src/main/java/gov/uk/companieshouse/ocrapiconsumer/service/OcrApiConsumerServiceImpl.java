package gov.uk.companieshouse.ocrapiconsumer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.logging.Logger;

import java.text.MessageFormat;

@Service
public class OcrApiConsumerServiceImpl implements OcrApiConsumerService {


    private static final String OCR_MICROSERVICE_ENDPOINT = "http://localhost:9090/dummy-endpoint";

    private Logger logger;
    private final RestTemplate restTemplate;


    @Autowired
    public OcrApiConsumerServiceImpl(Logger logger, RestTemplate restTemplate) {
        this.logger = logger;
        this.restTemplate = restTemplate;
    }

    @Override
    @Async
    public void logOcrRequest(String externalReferenceID, String imageEndpoint, String convertedTextEndpoint) {
        logger.info(MessageFormat.format("Received request with external reference id: {0}, image endpoint: {1}, converted text endpoint: {2}", externalReferenceID,imageEndpoint, convertedTextEndpoint));
    }

    @Override
    public ResponseEntity<String> sendRequestToOcrMicroservice(String externalReferenceID, String imageEndpoint, String convertedTextEndpoint) {
            HttpEntity<String> entity = new HttpEntity<>(externalReferenceID + imageEndpoint);
            return restTemplate.postForEntity(OCR_MICROSERVICE_ENDPOINT, entity, String.class);
    }
}
