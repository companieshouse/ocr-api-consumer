package uk.gov.companieshouse.ocrapiconsumer.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocr.OcrRequestMessage;
import uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication;

import java.io.IOException;

@Service
public class OcrApiConsumerService {

    private static final Logger LOG = LoggerFactory.getLogger(OcrApiConsumerApplication.APPLICATION_NAME_SPACE);
    private final OcrApiRequestAdapter ocrApiRequestAdapter;
    private final ChipsImageAdapter chipsImageAdapter;
    private final CallbackExtractedTextAdapter callbackExtractedTextAdapter;

    @Autowired
    public OcrApiConsumerService(OcrApiRequestAdapter ocrApiRequestAdapter,
                                 ChipsImageAdapter chipsImageAdapter,
                                 CallbackExtractedTextAdapter callbackExtractedTextAdapter) {
        this.ocrApiRequestAdapter = ocrApiRequestAdapter;
        this.chipsImageAdapter = chipsImageAdapter;
        this.callbackExtractedTextAdapter = callbackExtractedTextAdapter;
    }

    public void ocrRequest(OcrRequestMessage message) {
        orchestrateOcrRequest(message.getImageEndpoint(), message.getConvertedTextEndpoint(), message.getResponseId());
    }

    @Async
    public void logOcrRequest(String imageEndpoint, String convertedTextEndpoint, String responseId) {
        orchestrateOcrRequest(imageEndpoint, convertedTextEndpoint, responseId);
    }


    private void orchestrateOcrRequest(String imageEndpoint, String convertedTextEndpoint, String responseId) {
        LOG.infoContext(responseId,
                String.format("Request received with Image Endpoint: %s, Extracted Text Endpoint: %s",
                        imageEndpoint, convertedTextEndpoint),
                null);

        LOG.debugContext(responseId, "Getting the TIFF image", null);
        byte[] image = getTiffImage(imageEndpoint);

        LOG.debugContext(responseId, "Sending image to ocr microservice for conversion", null);

        ResponseEntity<ExtractTextResultDTO> response = sendRequestToOcrMicroservice(image, responseId);

        ExtractTextResultDTO extractedText = null;
        if (response != null) {
            extractedText = response.getBody();
        }

        LOG.debugContext(responseId,
                "Sending the extracted text response for the articles of association", null);
        sendTextResult(convertedTextEndpoint, extractedText);
    }

    public void sendOcrApiRequest(String responseId) {
        LOG.debugContext(responseId, "Creating byte array from test image", null);

        byte[] image = new byte[0];
        try {
            ClassPathResource classPathResource = new ClassPathResource("static/newer-articles-15.tif");
            image = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            LOG.info("See error log");
        }

        LOG.info("Length of byte array: " + image.length);
        LOG.debugContext(responseId, "Sending image to ocr microservice for conversion", null);

        ResponseEntity<ExtractTextResultDTO> response = sendRequestToOcrMicroservice(image, responseId);
        LOG.debugContext(responseId, "Processing time: " + response.getBody().getOcrProcessingTimeMs(), null);
        LOG.debugContext(responseId, "Total processing time: " + response.getBody().getTotalProcessingTimeMs(), null);
        LOG.debugContext(responseId, "Lowest confidence score: " + response.getBody().getLowestConfidenceScore(), null);
        LOG.debugContext(responseId, "Average confidence score: " + response.getBody().getAverageConfidenceScore(), null);
    }

    private byte[] getTiffImage(String imageEndpoint) {
        return chipsImageAdapter.getTiffImageFromChips(imageEndpoint);
    }

    private ResponseEntity<ExtractTextResultDTO> sendRequestToOcrMicroservice(byte[] image, String responseId) {
        return ocrApiRequestAdapter.sendOcrRequestToOcrApi(image, responseId);

    }

    private void sendTextResult(String extractedTextEndpoint, ExtractTextResultDTO extractedText) {
        callbackExtractedTextAdapter.sendTextResult(extractedTextEndpoint, extractedText);
    }

}
