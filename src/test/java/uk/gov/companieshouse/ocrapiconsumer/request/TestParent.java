package uk.gov.companieshouse.ocrapiconsumer.request;

import org.springframework.http.ResponseEntity;

public class TestParent {
    protected static final String CONTEXT_ID = "XYZ";
    protected static final String RESPONSE_ID = "ABC";
    protected static final String IMAGE_ENDPOINT = "https://image-endpoint";
    protected static final String EXTRACTED_TEXT_ENDPOINT = "https://converted-text-endpoint";
    protected static final byte[] MOCK_TIFF_CONTENT = {0, 1, 2};
    protected static final String EXTRACTED_TEXT = "Mock converted text";
    protected static final long OCR_PROCESSING_TIME = 100L;
    protected static final long TOTAL_PROCESSING_TIME = 200L;
    protected static final int LOWEST_CONFIDENCE_SCORE = 50;
    protected static final int AVERAGE_CONFIDENCE_SCORE = 75;
    protected ExtractTextResultDTO extractTextResultDTO;
    protected ResponseEntity<ExtractTextResultDTO> response;
    protected OcrRequestDTO ocrRequestDTO;

    protected ExtractTextResultDTO createMockTextResult() {
        ExtractTextResultDTO extractTextResultDTO = new ExtractTextResultDTO();
        extractTextResultDTO.setResponseId(CONTEXT_ID);
        extractTextResultDTO.setLowestConfidenceScore(LOWEST_CONFIDENCE_SCORE);
        extractTextResultDTO.setAverageConfidenceScore(AVERAGE_CONFIDENCE_SCORE);
        extractTextResultDTO.setExtractedText(EXTRACTED_TEXT);
        extractTextResultDTO.setOcrProcessingTimeMs(OCR_PROCESSING_TIME);
        extractTextResultDTO.setTotalProcessingTimeMs(TOTAL_PROCESSING_TIME);
        return extractTextResultDTO;
    }

    protected OcrRequestDTO createMockOcrRequest() {
        ocrRequestDTO = new OcrRequestDTO(IMAGE_ENDPOINT, EXTRACTED_TEXT_ENDPOINT, CONTEXT_ID);
        return ocrRequestDTO;
    }

}
