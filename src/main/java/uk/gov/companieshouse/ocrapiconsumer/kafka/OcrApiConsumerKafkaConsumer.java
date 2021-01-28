package uk.gov.companieshouse.ocrapiconsumer.kafka;

import static uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication.APPLICATION_NAME_SPACE;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocr.OcrRequestMessage;
import uk.gov.companieshouse.ocrapiconsumer.request.OcrApiConsumerService;

@Service
public class OcrApiConsumerKafkaConsumer {

    protected static final String OCR_REQUEST_TOPICS = "ocr-request";

    private static final Logger LOG = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private static final String OCR_REQUEST_GROUP = APPLICATION_NAME_SPACE + "-" + OCR_REQUEST_TOPICS;
    private static final String KAFKA_LISTENER_CONTAINER_FACTORY = "kafkaListenerContainerFactory";

    private OcrApiConsumerService ocrApiConsumerService;

    public OcrApiConsumerKafkaConsumer(OcrApiConsumerService ocrApiConsumerService) {
        this.ocrApiConsumerService = ocrApiConsumerService;
    }

    @KafkaListener(
        id = OCR_REQUEST_GROUP,
        topics = OCR_REQUEST_TOPICS,
        groupId = OCR_REQUEST_GROUP,
        autoStartup = "#{!${uk.gov.companieshouse.ocrapiconsumer.error-consumer}}",
        containerFactory = KAFKA_LISTENER_CONTAINER_FACTORY)
    public void consumeOcrApiRequestMessage(org.springframework.messaging.Message<OcrRequestMessage> message) {

        try {

            ocrApiConsumerService.ocrRequest(message.getPayload());

        } catch (Exception exception) {

        }
    }
}
