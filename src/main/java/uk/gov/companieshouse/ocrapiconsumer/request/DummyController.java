package uk.gov.companieshouse.ocrapiconsumer.request;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.ocrapiconsumer.request.extractedtext.ExtractTextResultDTO;

@RestController
public class DummyController {

    private static final String OCR_MICROSERVICE_ENDPOINT = "http://localhost:9090/dummy-endpoint";
    private static final String RESPONSE_ID_REQUEST_PARAMETER_NAME = "responseId";
    private static final String FILE_REQUEST_PARAMETER_NAME = "file";

    @GetMapping(OCR_MICROSERVICE_ENDPOINT)
    public HttpStatus dummyGetResponse() {
        return HttpStatus.OK;
    }

    @PostMapping(OCR_MICROSERVICE_ENDPOINT)
    public ResponseEntity<ExtractTextResultDTO> dummyPostResponse(
            @RequestParam(RESPONSE_ID_REQUEST_PARAMETER_NAME) String responseId,
            @RequestParam(FILE_REQUEST_PARAMETER_NAME) String file) {
        ExtractTextResultDTO result = new ExtractTextResultDTO();
        result.setAverageConfidenceScore(50);
        result.setLowestConfidenceScore(25);
        result.setOcrProcessingTimeMs(1000L);
        result.setTotalProcessingTimeMs(1300L);
        result.setResponseId("ABC");
        result.setExtractedText("This is dummy extracted text");
        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }
}
