package uk.gov.companieshouse.ocrapiconsumer.request;

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

    public byte[] getTiffImageFromChips(String imageEndpoint) {
        return restTemplate.getForEntity(imageEndpoint, byte[].class).getBody();
    }
}
