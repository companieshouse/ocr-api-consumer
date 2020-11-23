package uk.gov.companieshouse.ocrapiconsumer.request;

import org.springframework.http.ResponseEntity;

public class TestParent {
    protected static final String EXTERNAL_REFERENCE_ID = "ABC";
    protected static final String IMAGE_ENDPOINT = "https://image-endpoint";
    protected static final String EXTRACTED_TEXT_ENDPOINT = "https://extracted-text-endpoint";
    protected static final byte[] MOCK_TIFF_CONTENT = {0, 1, 2};
    protected static final String EXTRACTED_TEXT = "Mock converted text";
    protected static final long OCR_PROCESSING_TIME = 100L;
    protected static final long TOTAL_PROCESSING_TIME = 200L;
    protected static final int LOWEST_CONFIDENCE_SCORE = 50;
    protected static final int AVERAGE_CONFIDENCE_SCORE = 75;
    protected ExtractTextResultDTO extractTextResultDTO;
    protected ResponseEntity<ExtractTextResultDTO> response;

}
