package uk.gov.companieshouse.ocrapiconsumer.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication;
import uk.gov.companieshouse.ocrapiconsumer.kafka.exception.RetryableErrorException;

@Component
public class ImageRestClient {

    private static final Logger LOG = LoggerFactory.getLogger(OcrApiConsumerApplication.APPLICATION_NAME_SPACE);

    private final RestTemplate restTemplate;

    @Autowired
    public ImageRestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Gets a byte array representation of an image from the endpoint passed through.
     * @param   imageEndpoint   The endpoint that the image is retrieved from.
     * @return  A byte array of image contents used for the OCR text extraction.
     */
    public byte[] getImageContentsFromEndpoint(String contextId, String imageEndpoint) {
        
        LOG.debugContext(contextId, "Image from [" + imageEndpoint + "]",null);
        try {
            return restTemplate.getForEntity(imageEndpoint, byte[].class).getBody();

        } catch (Exception e) {
             throw new RetryableErrorException("Fail to get Image file from requesting system url ["
                     + imageEndpoint + "], error message [" + e.getMessage() + "]", e);
        }
    }
}
