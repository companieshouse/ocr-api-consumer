package uk.gov.companieshouse.ocrapiconsumer.kafka;

import static uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication.APPLICATION_NAME_SPACE;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
import uk.gov.companieshouse.ocrapiconsumer.kafka.exception.FatalErrorException;
import uk.gov.companieshouse.ocrapiconsumer.kafka.exception.MaximumRetriesException;
import uk.gov.companieshouse.ocrapiconsumer.kafka.exception.RetryableErrorException;
import uk.gov.companieshouse.ocrapiconsumer.request.OcrApiConsumerService;


@Service
public class OcrApiConsumerKafkaConsumer {

    protected static final String OCR_REQUEST_TOPICS = "ocr-request";
    protected static final String OCR_REQUEST_RETRY_TOPICS = "ocr-request-retry";

    private static final String OCR_REQUEST_KEY_RETRY = OCR_REQUEST_RETRY_TOPICS;

    private static final Logger LOG = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private static final String OCR_REQUEST_GROUP = APPLICATION_NAME_SPACE + "-" + OCR_REQUEST_TOPICS;
    private static final String OCR_REQUEST_RETRY_GROUP = APPLICATION_NAME_SPACE + "-" + OCR_REQUEST_RETRY_TOPICS;

    private static final String KAFKA_LISTENER_CONTAINER_FACTORY =   "kafkaListenerContainerFactory";

    private static final int MAX_RETRY_ATTEMPTS = 3;

    private final Map<String, Integer> retryCounts;

    private OcrApiConsumerService ocrApiConsumerService;
    private OcrMessageErrorHandler ocrMessageErrorHandler;
    private SerializerFactory serializerFactory;
    private OcrApiConsumerKafkaProducer kafkaProducer;

    @Autowired
    public OcrApiConsumerKafkaConsumer(SerializerFactory serializerFactory, OcrApiConsumerKafkaProducer kafkaProducer, final OcrApiConsumerService ocrApiConsumerService, OcrMessageErrorHandler ocrMessageErrorHandler) {

        this.retryCounts = new ConcurrentHashMap<>();
        this.serializerFactory = serializerFactory;
        this.kafkaProducer = kafkaProducer;
        this.ocrApiConsumerService = ocrApiConsumerService;
        this.ocrMessageErrorHandler = ocrMessageErrorHandler;
    }

    @KafkaListener(
        id = OCR_REQUEST_GROUP,
        topics = OCR_REQUEST_TOPICS,
        groupId = OCR_REQUEST_GROUP,
        topicPartitions =
        { @TopicPartition(topic = OCR_REQUEST_TOPICS, partitions = { "0-2" }),
        },
        containerFactory = KAFKA_LISTENER_CONTAINER_FACTORY)
    public void consumeOcrApiRequestMessage(org.springframework.messaging.Message<OcrRequestMessage> message, ConsumerRecordMetadata metadata) {

        logConsumeKafkaMessage(message.getPayload().getResponseId(), metadata);

        handleOcrRequestMessage(message, metadata.topic());
    }


    @KafkaListener(
        id = OCR_REQUEST_RETRY_GROUP,
        topics = OCR_REQUEST_RETRY_TOPICS,
        groupId = OCR_REQUEST_RETRY_GROUP,
        containerFactory = KAFKA_LISTENER_CONTAINER_FACTORY)
    public void consumeOcrApiRequestRetryMessage(org.springframework.messaging.Message<OcrRequestMessage> message, ConsumerRecordMetadata metadata) {
        
        logConsumeKafkaMessage(message.getPayload().getResponseId(), metadata);

        handleOcrRequestMessage(message, metadata.topic());
    }


    private void handleOcrRequestMessage(org.springframework.messaging.Message<OcrRequestMessage> message,
            String topicName) {

        OcrRequestMessage ocrRequestMessage = message.getPayload();
        String contextId = ocrRequestMessage.getResponseId();

        try {

            ocrApiConsumerService.ocrRequest(ocrRequestMessage);

            resetKeyFromRetryCounts(contextId); // must be last statement before error handling

        } catch (RetryableErrorException ree) {
            
            LOG.errorContext(contextId, "Retryable Error consuming message", ree, null);
            
            try {
                retryMessage(contextId, message, topicName);

            } catch (MaximumRetriesException mre) {
                resetKeyFromRetryCounts(contextId);
                ocrMessageErrorHandler.handleMaximumRetriesException(contextId, mre);

            } catch (Exception ex) {
                resetKeyFromRetryCounts(contextId);
                ocrMessageErrorHandler.generalExceptionAfterRetry(contextId, ex);
            }
        } catch (Exception exception) {
            resetKeyFromRetryCounts(contextId);
            ocrMessageErrorHandler.generalException(contextId, exception);

        }
    }

    private void retryMessage(String contextId, org.springframework.messaging.Message<OcrRequestMessage> message, String currentTopic) {

        if (currentTopic.equals(getMainTopicName())) {

            // TODO delay Jira

            repostMessage(contextId, message.getPayload(), currentTopic, getRetryTopicName());

        }  else if (currentTopic.equals(getRetryTopicName())) {

            int retryCount = retryCounts.getOrDefault(contextId, 1);

            if (retryCount >= getMaxRetryAttempts()) {

                throw new MaximumRetriesException();

            } else {

                retryCounts.put(contextId, retryCounts.getOrDefault(contextId, 0) + 1);

                // TODO delay Jira

                LOG.infoContext(contextId, "Retrying processing message [count: " + retryCounts.get(contextId) + "]", null);
                handleOcrRequestMessage(message, currentTopic);
            }

        } else {
          
           throw new FatalErrorException("Logic error in code");
        }
    }


    private void resetKeyFromRetryCounts(String counterKey) {
        retryCounts.remove(counterKey);
    }

    private void repostMessage(String contextId, final OcrRequestMessage ocrRequestMessage, final String fromTopic, final String toTopic) {

        Message retryMessage = createRepostMessage(ocrRequestMessage, toTopic);

        String failureMessage = "Can not repost message";
        LOG.infoContext(contextId, "Reposting message from topic [" + fromTopic + "]" + " to topic [" + toTopic + "]", null);

        try {
            kafkaProducer.sendMessage(retryMessage);

        } catch (InterruptedException ie) {

            LOG.error(failureMessage, ie);

            Thread.currentThread().interrupt();

        }  catch (ExecutionException ee) {

            LOG.error(failureMessage, ee);
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

    protected String getMainTopicName() {
        return OCR_REQUEST_TOPICS;
    }

    String getRetryTopicName() {
        return OCR_REQUEST_RETRY_TOPICS;
    }

    private int getMaxRetryAttempts() {
        return MAX_RETRY_ATTEMPTS;
    }

    // Use for unit testing
    protected Map<String, Integer> getRetryCounts() {
        return retryCounts;
    }

    // logging helper methods
    private void logConsumeKafkaMessage(String contextId, ConsumerRecordMetadata meta) {

        LOG.infoContext(contextId, "Consuming Message from offset [" + meta.offset() + "] on topic [" + meta.topic() + "] partition [" + meta.partition() + "] thread id [" + Thread.currentThread().getId() + "]", null);
    }

}
