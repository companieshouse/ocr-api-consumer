package uk.gov.companieshouse.ocrapiconsumer.request;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class is the extracted text result returned by the ocr-api
 */
public class ExtractTextResultDTO {

    protected static final String OCR_CONVERSION_ERROR_TEXT = "UNABLE_TO_PROCESS_OCR_CONVERSION";
    protected static final int OCR_CONVERSION_ERROR_CODE = 1;

    /**
     * Creates an extracted text result with default values and the context id, for use in non-retryable errors.
     * @param contextId     The context ID of the application.
     * @param responseId    The response ID of the application.
     * @return              An ExtractTextResultDTO object with default values.
     */
    static ExtractTextResultDTO createErrorExtractTextResultDTOFromContextId(String contextId, String responseId) {
        ExtractTextResultDTO extractedTextError = new ExtractTextResultDTO();
        extractedTextError.setContextId(contextId);
        extractedTextError.setAverageConfidenceScore(0);
        extractedTextError.setLowestConfidenceScore(0);
        extractedTextError.setOcrProcessingTimeMs(0L);
        extractedTextError.setTotalProcessingTimeMs(0L);
        extractedTextError.setResponseId(responseId);
        extractedTextError.setExtractedText(OCR_CONVERSION_ERROR_TEXT);
        extractedTextError.setResultCode(OCR_CONVERSION_ERROR_CODE);
        return extractedTextError;
    }

    /**
     *  The input CHS contextId of the OCR request
     */
    @JsonProperty("context_id")
    private String contextId;

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

	/**
	 * To store the error code/result of the OCR Service. The values will be:
	 *  0 = SUCCESS
	 * -1 = TIMEOUT BY CHIPS
	 * >0 = ERROR FROM ocr service
	 */
	@JsonProperty("result_code")
	private int resultCode;

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

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

    public int getResultCode() {
		return resultCode;
	}

	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}

	public Map<String,Object>  metadataMap() {

        Map<String,Object> map = new LinkedHashMap<>();

        map.put("lowest_confidence_score", lowestConfidenceScore);
        map.put("average_confidence_score", averageConfidenceScore);
        map.put("ocr_processing_time_ms", ocrProcessingTimeMs);
        map.put("total_processing_time_ms", totalProcessingTimeMs);
        map.put("response_id", responseId);
        map.put("context_id", contextId);
        map.put("result_code", resultCode);

        return map;        
    }

}
