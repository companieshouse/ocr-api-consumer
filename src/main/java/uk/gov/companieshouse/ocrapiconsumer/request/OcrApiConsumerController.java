package uk.gov.companieshouse.ocrapiconsumer.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication;
import uk.gov.companieshouse.ocrapiconsumer.common.ErrorResponseDTO;

@RestController
public class OcrApiConsumerController {

    private static final String REQUEST_ENDPOINT = "/ocr-requests";
    private static final String IMAGE_ENDPOINT_PARAMETER_NAME = "image_endpoint";
    private static final String CONVERTED_TEXT_ENDPOINT_PARAMETER_NAME = "converted_text_endpoint";
    private static final String RESPONSE_ID_PARAMETER_NAME = "response_id";

    private static final String CONTROLLER_ERROR_MESSAGE = "Unexpected error occurred";
    private static final String CLIENT_ERROR_MESSAGE = "A client error has occurred during the request";
    private static final String SERVER_ERROR_MESSAGE = "A server error has occurred during the request";

    private static final Logger LOG = LoggerFactory.getLogger(OcrApiConsumerApplication.APPLICATION_NAME_SPACE);

    private final OcrApiConsumerService service;

    @Autowired
    public OcrApiConsumerController(final OcrApiConsumerService service) {
        this.service = service;
    }

    /**
     * Receives an OCR request from CHIPS and calls the service to:
     * - log it asynchronously
     * - return status code 202 (ACCEPTED)
     * @param   imageEndpoint             The endpoint that the image is located at
     * @param   convertedTextEndpoint     The endpoint to send the converted text to
     * @param   responseId                The response ID of the request
     * @return                            The HTTP Status code 202 ACCEPTED
     */
    @PostMapping(REQUEST_ENDPOINT)
    public ResponseEntity<HttpStatus> receiveOcrRequest(@RequestParam(IMAGE_ENDPOINT_PARAMETER_NAME) String imageEndpoint,
                                                        @RequestParam(CONVERTED_TEXT_ENDPOINT_PARAMETER_NAME) String convertedTextEndpoint,
                                                        @RequestParam(RESPONSE_ID_PARAMETER_NAME) String responseId) {

        service.logOcrRequest(imageEndpoint, convertedTextEndpoint, responseId);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    /**
     * Handles errors for any rest requests.
     * @param e The runtime exception thrown by rest template requests.
     * @return  A response entity containing the error response and http code returned by rest template call.
     */
    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ErrorResponseDTO> restClientException(Exception e) {
        var errorResponse = new ErrorResponseDTO();
        HttpStatus statusCode = HttpStatus.INTERNAL_SERVER_ERROR;

        if(e instanceof HttpClientErrorException) {
            var cause = (HttpClientErrorException) e.getCause();
            statusCode = cause.getStatusCode();
            errorResponse.setErrorMessage(CLIENT_ERROR_MESSAGE);
            LOG.error(null, e);

            errorResponse.setErrorMessage(CLIENT_ERROR_MESSAGE);
        } else if(e instanceof HttpServerErrorException) {
            var cause = (HttpServerErrorException) e.getCause();
            statusCode = cause.getStatusCode();
            errorResponse.setErrorMessage(SERVER_ERROR_MESSAGE);
            LOG.error(null, e);
        } else {
            errorResponse.setErrorMessage(CONTROLLER_ERROR_MESSAGE);
        }

        return new ResponseEntity<>(errorResponse, statusCode);
    }

    /**
     * Catches any uncaught exception.
     * @param e The exception thrown
     * @return  A Response Entity containing the error response and a 500 code.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> uncaughtException(Exception e) {

        LOG.error(null, e);

        var errorResponse = new ErrorResponseDTO();
        errorResponse.setErrorMessage(CONTROLLER_ERROR_MESSAGE);

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
