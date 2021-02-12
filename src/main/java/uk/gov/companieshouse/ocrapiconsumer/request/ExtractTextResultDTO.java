package uk.gov.companieshouse.ocrapiconsumer.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class is the extracted text result returned by the ocr-api
 * Can import this class from the ocr-api for drop 2
 */
public class ExtractTextResultDTO {

    protected static final String OCR_CONVERSION_ERROR_TEXT = "UNABLE_TO_PROCESS_OCR_CONVERSION";

    /**
     * Creates an extracted text result with default values and the context id, for use in non-retryable errors.
     * @param contextId The context ID of the application.
     * @return          An ExtractTextResultDTO object with default values.
     */
    static ExtractTextResultDTO createErrorExtractTextResultDTOFromContextId(String contextId) {
        ExtractTextResultDTO extractedTextError = new ExtractTextResultDTO();
        extractedTextError.setAverageConfidenceScore(0);
        extractedTextError.setLowestConfidenceScore(0);
        extractedTextError.setOcrProcessingTimeMs(0L);
        extractedTextError.setTotalProcessingTimeMs(0L);
        extractedTextError.setResponseId(contextId);
        extractedTextError.setExtractedText(OCR_CONVERSION_ERROR_TEXT);
        return extractedTextError;
    }

    /**
     *  The text for the OCR request
     */
    @JsonProperty("extracted_text")
    private String extractedText;

    /**
     *  average value (line by line) of the Tesseract confidence (0 to 100) of the OCR conversion process
     */
    @JsonProperty("average_confidence_score")
    private int averageConfidenceScore;

    /**
     *  the lowest value (line by line) of the Tesseract confidence (0 to 100) of the OCR conversion process
     */
    @JsonProperty("lowest_confidence_score")
    private int lowestConfidenceScore;

    /**
     *  Time for the OCT Text conversion process itself
     */
    @JsonProperty("ocr_processing_time_ms")
    private long ocrProcessingTimeMs;

    @JsonProperty("total_processing_time_ms")
    private long totalProcessingTimeMs;

    /**
     *  The input responseId of the OCR request
     */
    @JsonProperty("response_id")
    private String responseId;

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public int getAverageConfidenceScore() {
        return averageConfidenceScore;
    }

    public void setAverageConfidenceScore(int averageConfidenceScore) {
        this.averageConfidenceScore = averageConfidenceScore;
    }

    public int getLowestConfidenceScore() {
        return lowestConfidenceScore;
    }

    public void setLowestConfidenceScore(int lowestConfidenceScore) {
        this.lowestConfidenceScore = lowestConfidenceScore;
    }

    public long getOcrProcessingTimeMs() {
        return ocrProcessingTimeMs;
    }

    public void setOcrProcessingTimeMs(long ocrProcessingTimeMs) {
        this.ocrProcessingTimeMs = ocrProcessingTimeMs;
    }

    public long getTotalProcessingTimeMs() {
        return totalProcessingTimeMs;
    }

    public void setTotalProcessingTimeMs(long totalProcessingTimeMs) {
        this.totalProcessingTimeMs = totalProcessingTimeMs;
    }

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

}