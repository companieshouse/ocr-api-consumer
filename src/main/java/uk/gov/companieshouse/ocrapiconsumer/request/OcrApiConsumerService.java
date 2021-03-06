package uk.gov.companieshouse.ocrapiconsumer.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocr.OcrRequestMessage;
import uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication;

import java.util.LinkedHashMap;
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
        orchestrateOcrRequest(new OcrRequestDTO(message));
    }

    @Async
    public void processOcrRequest(OcrRequestDTO ocrRequestDTO) {
        orchestrateOcrRequest(ocrRequestDTO);
    }


    private void orchestrateOcrRequest(OcrRequestDTO ocrRequestDTO) {

        LOG.infoContext(ocrRequestDTO.getContextId(),
                String.format("Request received with Image Endpoint: %s, Extracted Text Endpoint: %s",
                    ocrRequestDTO.getImageEndpoint(),
                    ocrRequestDTO.getConvertedTextEndpoint()),
                    null);

        LOG.debugContext(ocrRequestDTO.getContextId(), "Getting the TIFF image", null);
        byte[] image = getImageContents(ocrRequestDTO);

        LOG.debugContext(ocrRequestDTO.getContextId(), "Sending image to ocr microservice for conversion", imageLogMap(image));

        ResponseEntity<ExtractTextResultDTO> response
                = sendRequestToOcrMicroservice(ocrRequestDTO.getContextId(), image, ocrRequestDTO.getResponseId());

        ExtractTextResultDTO extractedText = null;
        Map<String, Object> metadata = null;
        if (response != null) {
            extractedText = response.getBody();
            metadata = extractedText != null ? extractedText.metadataMap() : null;
        }

        LOG.debugContext(ocrRequestDTO.getContextId(),
                "Sending the extracted text response for the articles of association", metadata);
        sendTextResult(ocrRequestDTO, extractedText);
    }


    private Map<String, Object> imageLogMap(byte[] image) {

        Map<String,Object> map = new LinkedHashMap<>();

        map.put("image_length", image.length);
  
        return map;
    }

    private byte[] getImageContents(OcrRequestDTO ocrRequestDTO) {
        return imageRestClient.getImageContentsFromEndpoint(ocrRequestDTO.getContextId(), ocrRequestDTO.getImageEndpoint());
    }

    private ResponseEntity<ExtractTextResultDTO> sendRequestToOcrMicroservice(String contextId, byte[] image, String responseId) {
        return ocrApiRequestRestClient.obtainExtractTextResult(contextId, image, responseId);

    }

    private void sendTextResult(OcrRequestDTO ocrRequestDTO, ExtractTextResultDTO extractedText) {
        callbackExtractedTextRestClient.sendTextResult(ocrRequestDTO.getConvertedTextEndpoint(), extractedText);
    }

}
