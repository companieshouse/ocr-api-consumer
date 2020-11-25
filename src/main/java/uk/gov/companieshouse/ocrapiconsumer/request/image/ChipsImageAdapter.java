package uk.gov.companieshouse.ocrapiconsumer.request.image;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication;

import java.io.IOException;

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
     * @param   imageEndpoint               The endpoint that the image is retrieved from.
     * @return  A byte array of tiff image contents used for the OCR text extraction.
     * @throws  IOException  When the endpoint returns a 404 NOT FOUND.
     */
    public byte[] getTiffImageFromChips(String imageEndpoint) throws IOException {

        ResponseEntity<byte[]> responseEntity = restTemplate.getForEntity(imageEndpoint, byte[].class);

        if(responseEntity.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            IOException exception = new IOException();
            LOG.error(exception);
            throw exception;
        }

        return responseEntity.getBody();
    }
}
