package uk.gov.companieshouse.ocrapiconsumer.request.image;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ChipsImageAdapter {

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
        return restTemplate.getForEntity(imageEndpoint, byte[].class).getBody();
    }
}
