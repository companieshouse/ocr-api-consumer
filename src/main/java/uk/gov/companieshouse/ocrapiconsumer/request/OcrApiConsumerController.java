package uk.gov.companieshouse.ocrapiconsumer.request;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    private static final String REQUEST_ENDPOINT = "/internal/ocr-requests";

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
     * @param   ocrRequest  A request object containing the 3 mandatory JSON fields and one optional field (context id)
     * @return              The HTTP Status code 202 ACCEPTED
     */
    @PostMapping(REQUEST_ENDPOINT)
    public ResponseEntity<HttpStatus> receiveOcrRequest(@Valid @RequestBody OcrRequest ocrRequest) {

        service.logOcrRequest(ocrRequest);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    /**
     * Sends a standard (packaged) image to ocr_request as a basic test without needing a client system such
     * as CHIPS initialiating a call.
     * @param ocrRequest
     * @return HTTP 200 OK
     */
    @PostMapping("/internal/ocr-api-request")
    public ResponseEntity<HttpStatus> sendTestOcrApiRequest(@Valid @RequestBody OcrRequest ocrRequest) {
        String version = System.getProperty("java.version");
        String responseId = ocrRequest.getResponseId();
        LOG.debugContext(responseId, "Java version: " + version, null);

        service.sendOcrApiRequestForStandardTiff(responseId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Handles errors for any rest requests.
     * @param e The runtime exception thrown by rest template requests.
     * @return  A response entity containing the error response and http code returned by rest template call.
     */
    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ErrorResponseDTO> restClientException(Exception e) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO();
        HttpStatus statusCode = HttpStatus.INTERNAL_SERVER_ERROR;

        if(e instanceof HttpClientErrorException) {
            HttpClientErrorException cause = (HttpClientErrorException) e.getCause();
            statusCode = cause.getStatusCode();
            errorResponse.setErrorMessage(CLIENT_ERROR_MESSAGE);
            LOG.error(null, e);

            errorResponse.setErrorMessage(CLIENT_ERROR_MESSAGE);
        } else if(e instanceof HttpServerErrorException) {
            HttpServerErrorException cause = (HttpServerErrorException) e.getCause();
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

        ErrorResponseDTO errorResponse = new ErrorResponseDTO();
        errorResponse.setErrorMessage(CONTROLLER_ERROR_MESSAGE);

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
