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
    public void logOcrRequest(String externalReferenceID, String imageEndpoint, String extractedTextEndpoint) {
        LOG.infoContext(externalReferenceID,
                String.format("Request received with ID: %s, Image Endpoint: %s, Extractedß Text Endpoint: %s",
                        externalReferenceID, imageEndpoint, extractedTextEndpoint),
                null);

        LOG.debugContext(externalReferenceID, "Getting the TIFF image", null);
        byte[] image = getTiffImage(imageEndpoint);

        LOG.debugContext(externalReferenceID, "Sending image to ocr microservice for conversion", null);
        ExtractTextResultDTO extractedText = sendRequestToOcrMicroservice(externalReferenceID, image).getBody();

        LOG.debugContext(externalReferenceID, "Sending the extracted text response for the articles of association", null);
        sendTextResult(extractedTextEndpoint, extractedText);
    }

    private byte[] getTiffImage(String imageEndpoint) {
        return chipsImageAdapter.getTiffImageFromChips(imageEndpoint);
    }

    private ResponseEntity<ExtractTextResultDTO> sendRequestToOcrMicroservice(String externalReferenceID, byte[] image) {
        return ocrApiRequestAdapter.sendOcrRequestToOcrApi(externalReferenceID, image);
    }

    private void sendTextResult(String convertedTextEndpoint, ExtractTextResultDTO extractedText) {
        chipsExtractedTextAdapter.sendTextResult(convertedTextEndpoint, extractedText);
    }

}
