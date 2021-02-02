package uk.gov.companieshouse.ocrapiconsumer.kafka;

import static uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication.APPLICATION_NAME_SPACE;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.listener.adapter.ConsumerRecordMetadata;
import org.springframework.stereotype.Service;

import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocr.OcrRequestMessage;
import uk.gov.companieshouse.ocrapiconsumer.kafka.exception.RetryableErrorException;
import uk.gov.companieshouse.ocrapiconsumer.request.OcrApiConsumerService;


@Service
public class OcrApiConsumerKafkaConsumer {

    protected static final String OCR_REQUEST_TOPICS = "ocr-request";
    protected static final String OCR_REQUEST_RETRY_TOPICS = "ocr-request-retry";

    private static final String OCR_REQUEST_KEY_RETRY = OCR_REQUEST_RETRY_TOPICS;

    private static final Logger LOG = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private static final String OCR_REQUEST_GROUP = APPLICATION_NAME_SPACE + "-" + OCR_REQUEST_TOPICS;
    private static final String KAFKA_LISTENER_CONTAINER_FACTORY =   "kafkaListenerContainerFactory";

    private OcrApiConsumerService ocrApiConsumerService;
    private SerializerFactory serializerFactory;
    private OcrApiConsumerKafkaProducer kafkaProducer;

    @Autowired
    public OcrApiConsumerKafkaConsumer(SerializerFactory serializerFactory, OcrApiConsumerKafkaProducer kafkaProducer, final OcrApiConsumerService ocrApiConsumerService) {

        this.serializerFactory = serializerFactory;
        this.kafkaProducer = kafkaProducer;
        this.ocrApiConsumerService = ocrApiConsumerService;
    }

    @KafkaListener(
        id = OCR_REQUEST_GROUP,
        topics = OCR_REQUEST_TOPICS,
        groupId = OCR_REQUEST_GROUP,
        topicPartitions =
        { @TopicPartition(topic = OCR_REQUEST_TOPICS, partitions = { "0-2" }),
        },
        autoStartup = "#{!${uk.gov.companieshouse.ocrapiconsumer.error-consumer}}",
        containerFactory = KAFKA_LISTENER_CONTAINER_FACTORY)
    public void consumeOcrApiRequestMessage(org.springframework.messaging.Message<OcrRequestMessage> message, ConsumerRecordMetadata meta) {

        OcrRequestMessage ocrRequestMessage = message.getPayload();

        LOG.infoContext(ocrRequestMessage.getResponseId(), "Consuming Message from offset [" + meta.offset() + "] on topic [" + meta.topic() + "] partition [" + meta.partition() + "]", null);

        try {

            ocrApiConsumerService.ocrRequest(ocrRequestMessage);

        } catch (RetryableErrorException ree) {
            
            LOG.errorContext(ocrRequestMessage.getResponseId(), "Retryable Error consuming message", ree, null);

            repostMessage(ocrRequestMessage, OCR_REQUEST_RETRY_TOPICS);

        } catch (Exception exception) {
            LOG.errorContext(ocrRequestMessage.getResponseId(), "Unexpected Error when consuming message", exception, null);
        }
    }

    private void repostMessage(final OcrRequestMessage ocrRequestMessage, final String topic) {

        Message retryMessage = createRepostMessage(ocrRequestMessage, topic);

        try {
            kafkaProducer.sendMessage(retryMessage);

        } catch (ExecutionException | InterruptedException exception) {

            LOG.error("Can not repost message", exception);

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
            LOG.error("Can not serialise message", exception);
        }

        retryMessage.setTopic(topic);
        retryMessage.setTimestamp(new Date().getTime());

        return retryMessage;
    }
}
