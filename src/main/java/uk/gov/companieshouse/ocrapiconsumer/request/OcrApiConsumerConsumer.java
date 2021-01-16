package uk.gov.companieshouse.ocrapiconsumer.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication;

@Service
public class OcrApiConsumerConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(OcrApiConsumerApplication.APPLICATION_NAME_SPACE);

    public static final String APPLICATION_NAMESPACE = "ocr-api-consumer";

    private static final String OCR_REQUEST_TOPICS = "ocr-request";
    private static final String OCR_REQUEST_RETRY_TOPICS = "ocr-request-retry";
    private static final String OCR_REQUEST_KEY_RETRY = OCR_REQUEST_RETRY_TOPICS;
    private static final String OCR_REQUEST_ERROR_TOPICS = "ocr-request-error";
    private static final String OCR_REQUEST_GROUP = APPLICATION_NAMESPACE + "-" + OCR_REQUEST_TOPICS;
    private static final String OCR_REQUEST_RETRY_GROUP = APPLICATION_NAMESPACE + "-" + OCR_REQUEST_RETRY_TOPICS;
    private static final String OCR_REQUEST_ERROR_GROUP = APPLICATION_NAMESPACE + "-" + OCR_REQUEST_ERROR_TOPICS;

    private static final int MAX_RETRY_ATTEMPTS = 3;

    private static long errorRecoveryOffset = 0L;

    private final OcrApiConsumerService ocrApiConsumerService;

    @Autowired
    public OcrApiConsumerConsumer(final OcrApiConsumerService ocrApiConsumerService) {
        this.ocrApiConsumerService = ocrApiConsumerService;
    }

    @KafkaListener(
        id = OCR_REQUEST_GROUP,
        topics = OCR_REQUEST_TOPICS,
        groupId = OCR_REQUEST_GROUP,
        autoStartup = "#{!${uk.gov.companieshouse.ocrapiconsumer.error-consumer}}",
        containerFactory = "kafkaListenerContainerFactory")
    public void processOcrApiRequest(String message) {
        
        LOG.debug("Received Messasge in group - group-id: " + message);

        boolean doKafka = false;
        if (doKafka) {
            ocrApiConsumerService.sendOcrApiRequest(message);
        }
    }

    @KafkaListener(
        id = OCR_REQUEST_RETRY_GROUP,
        topics = OCR_REQUEST_RETRY_TOPICS,
        groupId = OCR_REQUEST_RETRY_GROUP,
        autoStartup = "#{!${uk.gov.companieshouse.ocrapiconsumer.error-consumer}}",
        containerFactory = "kafkaListenerContainerFactory")
    public void retryOcrApiRequest(org.springframework.messaging.Message<OcrKafkaRequest> message) {
        // RETRY
    }

    @KafkaListener(
        id = OCR_REQUEST_ERROR_GROUP,
        topics = OCR_REQUEST_ERROR_TOPICS,
        groupId = OCR_REQUEST_ERROR_GROUP,
        autoStartup = "#{!${uk.gov.companieshouse.ocrapiconsumer.error-consumer}}",
        containerFactory = "kafkaListenerContainerFactory")
    public void errorOcrApiRequest(org.springframework.messaging.Message<OcrKafkaRequest> message) {
        // RETRY
    }
}
