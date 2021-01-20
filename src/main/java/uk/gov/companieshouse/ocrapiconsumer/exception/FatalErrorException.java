package uk.gov.companieshouse.ocrapiconsumer.exception;

import uk.gov.companieshouse.ocr.OcrRequestMessage;

/**
 * Thrown to indicate a non recoverable error in processing that can NOT be tried again. An example of this is a null pointer that is unexpectedly thrown.
 */
public class FatalErrorException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private OcrRequestMessage ocrRequestMessage;

    public FatalErrorException(String message) {
        super(message);
    }

    public OcrRequestMessage getOcrRequestMessage() {
        return ocrRequestMessage;
    }

    public void setOcrRequestMessage(OcrRequestMessage ocrRequestMessage) {
        this.ocrRequestMessage = ocrRequestMessage;
    }

    
}
