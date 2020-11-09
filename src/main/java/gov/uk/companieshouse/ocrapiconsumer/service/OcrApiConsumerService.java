package gov.uk.companieshouse.ocrapiconsumer.service;

import org.springframework.http.ResponseEntity;

/**
 * Responsible for:
 * - logging requests asynchronously
 * - sends request to the ocr-microservice synchronously
 */
public interface OcrApiConsumerService {

    /**
     * Asynchronously logs OCR requests using the request details
     * @param externalReferenceID       The request reference ID
     * @param imageEndpoint             The endpoint of the image to perform the OCR on
     * @param convertedTextEndpoint     The endpoint of the converted text output
     */
    void logOcrRequest(String externalReferenceID, String imageEndpoint, String convertedTextEndpoint);

    /**
     * Synchronously sends the request to the OCR microservice
     * @param externalReferenceID       The request reference ID
     * @param imageEndpoint             The endpoint of the image to perform the OCR on
     * @param convertedTextEndpoint     The endpoint of the converted text output
     */
    ResponseEntity<String> sendRequestToOcrMicroservice(String externalReferenceID, String imageEndpoint, String convertedTextEndpoint);

}
