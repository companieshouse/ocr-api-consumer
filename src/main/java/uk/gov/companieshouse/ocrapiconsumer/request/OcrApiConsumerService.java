package uk.gov.companieshouse.ocrapiconsumer.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication;

@Service
public class OcrApiConsumerService {

    private static final Logger LOG = LoggerFactory.getLogger(OcrApiConsumerApplication.APPLICATION_NAME_SPACE);
    private final OcrApiRequestAdapter ocrApiRequestAdapter;
    private final ChipsImageAdapter chipsImageAdapter;
    private final ChipsExtractedTextAdapter chipsExtractedTextAdapter;

    @Autowired
    public OcrApiConsumerService(OcrApiRequestAdapter ocrApiRequestAdapter,
                                 ChipsImageAdapter chipsImageAdapter,
                                 ChipsExtractedTextAdapter chipsExtractedTextAdapter) {
        this.ocrApiRequestAdapter = ocrApiRequestAdapter;
        this.chipsImageAdapter = chipsImageAdapter;
        this.chipsExtractedTextAdapter = chipsExtractedTextAdapter;
    }

    @Async
    public void logOcrRequest(String imageEndpoint, String convertedTextEndpoint, String responseId) {
        LOG.infoContext(responseId,
                String.format("Request received with Image Endpoint: %s, Extracted Text Endpoint: %s",
                        imageEndpoint, convertedTextEndpoint),
                null);

        LOG.debugContext(responseId, "Getting the TIFF image", null);
        byte[] image = getTiffImage(imageEndpoint);

        LOG.debugContext(responseId, "Sending image to ocr microservice for conversion", null);

        ResponseEntity<ExtractTextResultDTO> response = sendRequestToOcrMicroservice(image, responseId);

        ExtractTextResultDTO extractedText = null;
        if(response != null) {
            extractedText = response.getBody();
        }

        LOG.debugContext(responseId,
                "Sending the extracted text response for the articles of association", null);
        sendTextResult(convertedTextEndpoint, extractedText);
    }

    private byte[] getTiffImage(String imageEndpoint) {
        return chipsImageAdapter.getTiffImageFromChips(imageEndpoint);
    }

    private ResponseEntity<ExtractTextResultDTO> sendRequestToOcrMicroservice(byte[] image, String responseId) {
        return ocrApiRequestAdapter.sendOcrRequestToOcrApi(image, responseId);

    }

    private void sendTextResult(String extractedTextEndpoint, ExtractTextResultDTO extractedText) {
        chipsExtractedTextAdapter.sendTextResult(extractedTextEndpoint, extractedText);
    }

}
