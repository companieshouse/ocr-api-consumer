package uk.gov.companieshouse.ocrapiconsumer.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class is the extracted text result returned by the ocr-api
 * Can import this class from the ocr-api for drop 2
 */
public class ExtractTextResultDTO {

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