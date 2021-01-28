package uk.gov.companieshouse.ocrapiconsumer.kafka;

import static uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication.APPLICATION_NAME_SPACE;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocr.OcrRequestMessage;
import uk.gov.companieshouse.ocrapiconsumer.exception.RetryableErrorException;
import uk.gov.companieshouse.ocrapiconsumer.kafka.OcrApiConsumerKafkaProducer;
import uk.gov.companieshouse.ocrapiconsumer.request.OcrApiConsumerService;


@Service
public class OcrApiConsumerKafkaConsumer {

    protected static final String OCR_REQUEST_TOPICS = "ocr-request";
    protected static final String OCR_REQUEST_RETRY_TOPICS = "ocr-request-retry";
    protected static final String OCR_REQUEST_ERROR_TOPICS = "ocr-request-error";

    private static final String OCR_REQUEST_KEY_RETRY = OCR_REQUEST_RETRY_TOPICS;

    private static final Logger LOG = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private static final String OCR_REQUEST_GROUP = APPLICATION_NAME_SPACE + "-" + OCR_REQUEST_TOPICS;
    private static final String KAFKA_LISTENER_CONTAINER_FACTORY = "kafkaListenerContainerFactory";

    private final Map<String, Integer> retryCount;

    private OcrApiConsumerService ocrApiConsumerService;
    private SerializerFactory serializerFactory;
    private OcrApiConsumerKafkaProducer kafkaProducer;

    public OcrApiConsumerKafkaConsumer(OcrApiConsumerService ocrApiConsumerService, SerializerFactory serializerFactory) {
        this.ocrApiConsumerService = ocrApiConsumerService;
        this.serializerFactory = serializerFactory;
    }

    @KafkaListener(
        id = OCR_REQUEST_GROUP,
        topics = OCR_REQUEST_TOPICS,
        groupId = OCR_REQUEST_GROUP,
        autoStartup = "#{!${uk.gov.companieshouse.ocrapiconsumer.error-consumer}}",
        containerFactory = KAFKA_LISTENER_CONTAINER_FACTORY)
    public void consumeOcrApiRequestMessage(org.springframework.messaging.Message<OcrRequestMessage> message) {

        OcrRequestMessage ocrRequestMessage = message.getPayload();

        try {

            ocrApiConsumerService.ocrRequest(ocrRequestMessage);

        } catch (RetryableErrorException exception) {
            
            // Log the exception

            repostMessage(ocrRequestMessage, OCR_REQUEST_RETRY_TOPICS);

        } catch (Exception exception) {
            // Log unrecoverable error
            throw exception;
        }
    }

    private void repostMessage(final OcrRequestMessage ocrRequestMessage, final String topic) {

        Message retryMessage = createRepostMessage(ocrRequestMessage, topic);

        try {
            kafkaProducer.sendMessage(retryMessage);

        } catch (ExecutionException | InterruptedException exception) {

            // Log error

            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private Message createRepostMessage(final OcrRequestMessage ocrRequestMessage, final String topic) {

        Message retryMessage = new Message();
        AvroSerializer<OcrRequestMessage> serializer = serializerFactory.getGenericRecordSerializer(OcrRequestMessage.class);

        retryMessage.setKey(OCR_REQUEST_KEY_RETRY);

        try {
            retryMessage.setValue(serializer.toBinary(ocrRequestMessage));
        } catch (SerializationException exception) {
            // Log Cannot serialize
        }

        retryMessage.setTopic(topic);
        retryMessage.setTimestamp(new Date().getTime());

        return retryMessage;
    }
}
