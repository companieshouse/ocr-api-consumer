package uk.gov.companieshouse.ocrapiconsumer.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication;
import uk.gov.companieshouse.ocrapiconsumer.common.ErrorResponseDTO;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping
public class OcrApiConsumerController {

    private static final Logger LOG = LoggerFactory.getLogger(OcrApiConsumerApplication.APPLICATION_NAME_SPACE);
    private static final String REQUEST_ENDPOINT = "/ocr-requests";
    private static final String EXTERNAL_REFERENCE_ID_PARAMETER_NAME = "external-reference-id";
    private static final String IMAGE_ENDPOINT_PARAMETER_NAME = "image-endpoint";
    private static final String CONVERTED_TEXT_ENDPOINT_PARAMETER_NAME = "converted-text-endpoint";

    private static final String CONTROLLER_ERROR_MESSAGE = "Unexpected Error Occurred";


    private final OcrApiConsumerService service;

    @Autowired
    public OcrApiConsumerController(final OcrApiConsumerService service) {
        this.service = service;
    }

    /**
     * Receives an OCR request from CHIPS and calls the service to:
     * - log it asynchronously
     * - get the image from CHIPS
     * - send the requestid and MultipartTiff to the ocr-microservice
     * - send the converted text to CHIPS
     * @param externalReferenceID       The request reference ID
     * @param imageEndpoint             The endpoint that the image is located at
     * @param convertedTextEndpoint     The endpoint to send the converted text to
     * @return                          The HTTP Status code 201 Created if successful
     */
    @PostMapping(REQUEST_ENDPOINT)
    public ResponseEntity<HttpStatus> receiveOcrRequest(@RequestParam(EXTERNAL_REFERENCE_ID_PARAMETER_NAME) String externalReferenceID,
                                                @RequestParam(IMAGE_ENDPOINT_PARAMETER_NAME) String imageEndpoint,
                                                @RequestParam(CONVERTED_TEXT_ENDPOINT_PARAMETER_NAME) String convertedTextEndpoint) {

        service.logOcrRequest(externalReferenceID, imageEndpoint, convertedTextEndpoint);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> uncaughtException(Exception e) {

        LOG.error(null, e);

        var errorResponse = new ErrorResponseDTO();
        errorResponse.setErrorMessage(CONTROLLER_ERROR_MESSAGE);

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
