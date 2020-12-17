package uk.gov.companieshouse.ocrapiconsumer.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication;

@Component
public class ChipsImageAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(OcrApiConsumerApplication.APPLICATION_NAME_SPACE);

    private final RestTemplate restTemplate;

    @Autowired
    public ChipsImageAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Gets a byte array of tiff contents from CHIPS.
     * @param   imageEndpoint   The endpoint that the image is retrieved from.
     * @return  A byte array of tiff image contents used for the OCR text extraction.
     */
    public byte[] getTiffImageFromChips(String imageEndpoint) {
        
        LOG.debug("Get Image from [" + imageEndpoint + "]");
        return restTemplate.getForEntity(imageEndpoint, byte[].class).getBody();
    }
}
