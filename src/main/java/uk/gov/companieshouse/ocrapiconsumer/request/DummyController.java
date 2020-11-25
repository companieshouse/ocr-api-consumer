package uk.gov.companieshouse.ocrapiconsumer.request;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.ocrapiconsumer.request.extractedtext.ExtractTextResultDTO;

@RestController
public class DummyController {

    private static final String FILE_REQUEST_PARAMETER_NAME = "file";
    private static final String RESPONSE_ID_REQUEST_PARAMETER_NAME = "responseId";

    @GetMapping("/image")
    public ResponseEntity<byte[]> dummyImageEndpoint() {
        byte[] bytes = {0, 1};
        return new ResponseEntity<>(bytes, HttpStatus.OK);
    }

    @PostMapping(value = "/ocr")
    public ResponseEntity<ExtractTextResultDTO> dummyOcrEndpoint(
            @RequestParam(FILE_REQUEST_PARAMETER_NAME) MultipartFile file,
            @RequestParam(RESPONSE_ID_REQUEST_PARAMETER_NAME) String responseId) {
        ExtractTextResultDTO result = new ExtractTextResultDTO();
        result.setAverageConfidenceScore(50);
        result.setLowestConfidenceScore(25);
        result.setOcrProcessingTimeMs(1000L);
        result.setTotalProcessingTimeMs(1300L);
        result.setResponseId("ABC");
        result.setExtractedText("This is dummy extracted text");
        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

    @PostMapping("/text")
    public ResponseEntity<String> dummyTextEndpoint() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
