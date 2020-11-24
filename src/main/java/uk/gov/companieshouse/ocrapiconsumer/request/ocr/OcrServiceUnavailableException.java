package uk.gov.companieshouse.ocrapiconsumer.request.ocr;

/**
 * Thrown when the OCR service is unavailable when sending a request.
 */
public class OcrServiceUnavailableException extends Exception {
    public OcrServiceUnavailableException() {
        super();
    }
}
