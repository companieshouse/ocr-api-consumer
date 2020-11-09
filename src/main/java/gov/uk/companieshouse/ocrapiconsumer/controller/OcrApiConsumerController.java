package gov.uk.companieshouse.ocrapiconsumer.controller;

import gov.uk.companieshouse.ocrapiconsumer.service.OcrApiConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.logging.Logger;

@RestController
@RequestMapping("ocr-request")
public class OcrApiConsumerController {

    private final OcrApiConsumerService service;
    private final Logger logger;

    @Autowired
    public OcrApiConsumerController(final OcrApiConsumerService service, final Logger logger) {
        this.service = service;
        this.logger = logger;
    }

    /**
     * Receives an OCR request from CHIPS and calls the service to:
     * - log it asynchronously
     * - send the request to the ocr-microservice
     * @param externalReferenceID       The request reference ID
     * @param imageEndpoint             The endpoint that the image is located at
     * @param convertedTextEndpoint     The endpoint to get the converted text from
     * @return                          The HTTP Status code 201 Created if successful
     */
    @PostMapping
    public HttpStatus receiveOcrRequest(@RequestParam(name = "external-reference-id") String externalReferenceID,
                                                @RequestParam(name = "image-endpoint") String imageEndpoint,
                                                @RequestParam(name = "converted-text-endpoint") String convertedTextEndpoint) {
        logger.debug("Request received");
        service.logOcrRequest(externalReferenceID, imageEndpoint, convertedTextEndpoint);
        ResponseEntity<String> response = service
                .sendRequestToOcrMicroservice(externalReferenceID, imageEndpoint, convertedTextEndpoint);

        return response.getStatusCode();
        // TODO: Check response to ensure it successfully sent the request
    }

}
