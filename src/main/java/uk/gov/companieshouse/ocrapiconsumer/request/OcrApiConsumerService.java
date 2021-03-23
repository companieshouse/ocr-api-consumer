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
import java.util.Map;

@Service
public class OcrApiConsumerService {

    private static final Logger LOG = LoggerFactory.getLogger(OcrApiConsumerApplication.APPLICATION_NAME_SPACE);
    private final OcrApiRequestRestClient ocrApiRequestRestClient;
    private final ImageRestClient imageRestClient;
    private final CallbackExtractedTextRestClient callbackExtractedTextRestClient;

    @Autowired
    public OcrApiConsumerService(OcrApiRequestRestClient ocrApiRequestRestClient,
                                 ImageRestClient imageRestClient,
                                 CallbackExtractedTextRestClient callbackExtractedTextRestClient) {
        this.ocrApiRequestRestClient = ocrApiRequestRestClient;
        this.imageRestClient = imageRestClient;
        this.callbackExtractedTextRestClient = callbackExtractedTextRestClient;
    }

    public void ocrRequest(OcrRequestMessage message) {
        orchestrateOcrRequest(new OcrRequest(message));
    }

    @Async
    public void processOcrRequest(OcrRequest ocrRequest) {
        orchestrateOcrRequest(ocrRequest);
    }


    private void orchestrateOcrRequest(OcrRequest ocrRequest) {

        LOG.infoContext(ocrRequest.getContextId(),
                String.format("Request received with Image Endpoint: %s, Extracted Text Endpoint: %s",
                    ocrRequest.getImageEndpoint(), 
                    ocrRequest.getConvertedTextEndpoint()),
                    null);

        LOG.debugContext(ocrRequest.getContextId(), "Getting the TIFF image", null);
        byte[] image = getImageContents(ocrRequest);

        LOG.debugContext(ocrRequest.getContextId(), "Sending image to ocr microservice for conversion", null);

        ResponseEntity<ExtractTextResultDTO> response
                = sendRequestToOcrMicroservice(ocrRequest.getContextId(), image, ocrRequest.getResponseId());

        ExtractTextResultDTO extractedText = null;
        Map<String, Object> metadata = null;
        if (response != null) {
            extractedText = response.getBody();
            metadata = extractedText != null ? extractedText.metadataMap() : null;
        }

        LOG.debugContext(ocrRequest.getContextId(),
                "Sending the extracted text response for the articles of association", metadata);
        sendTextResult(ocrRequest, extractedText);
    }

   

    private byte[] getImageContents(OcrRequest ocrRequest) {
        return imageRestClient.getImageContentsFromEndpoint(ocrRequest.getContextId(), ocrRequest.getImageEndpoint());
    }

    private ResponseEntity<ExtractTextResultDTO> sendRequestToOcrMicroservice(String contextId, byte[] image, String responseId) {
        return ocrApiRequestRestClient.sendOcrRequestToOcrApi(contextId, image, responseId);

    }

    private void sendTextResult(OcrRequest ocrRequest, ExtractTextResultDTO extractedText) {
        callbackExtractedTextRestClient.sendTextResult(ocrRequest.getConvertedTextEndpoint(), extractedText);
    }


    public void sendOcrApiRequestForStandardTiff(String responseId) {

        String contextId = responseId;

        LOG.debugContext(contextId, "Creating byte array from test image", null);

        byte[] image = new byte[0];
        try {
            ClassPathResource classPathResource = new ClassPathResource("static/newer-articles-15.tif");
            image = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            LOG.info("See error log");
        }

        LOG.info("Length of byte array: " + image.length);
        LOG.debugContext(contextId, "Sending image to ocr microservice for conversion", null);

        ResponseEntity<ExtractTextResultDTO> response = sendRequestToOcrMicroservice(contextId, image, responseId);
        ExtractTextResultDTO extractTextResult = response.getBody();

        if (extractTextResult != null) {
            LOG.debugContext(contextId, "Processing time: " + extractTextResult.getOcrProcessingTimeMs(), null);
            LOG.debugContext(contextId, "Total processing time: " + extractTextResult.getTotalProcessingTimeMs(),
                    null);
            LOG.debugContext(contextId, "Lowest confidence score: " + extractTextResult.getLowestConfidenceScore(),
                    null);
            LOG.debugContext(contextId, "Average confidence score: " + extractTextResult.getAverageConfidenceScore(),
                    null);
        } else {
            LOG.debugContext(contextId, "Null response body", null);
        }
    }
}
