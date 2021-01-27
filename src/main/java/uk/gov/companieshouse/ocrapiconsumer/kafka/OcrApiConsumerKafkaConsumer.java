package uk.gov.companieshouse.ocrapiconsumer.kafka;

import static uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication.APPLICATION_NAME_SPACE;
import static uk.gov.companieshouse.ocrapiconsumer.logging.LoggingUtils.logIfNotNull;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocr.OcrRequestMessage;
import uk.gov.companieshouse.ocrapiconsumer.exception.DuplicateErrorException;
import uk.gov.companieshouse.ocrapiconsumer.exception.FatalErrorException;
import uk.gov.companieshouse.ocrapiconsumer.exception.RetryableErrorException;
import uk.gov.companieshouse.ocrapiconsumer.logging.LoggingUtils;
import uk.gov.companieshouse.ocrapiconsumer.request.OcrApiConsumerService;

@Service
public class OcrApiConsumerKafkaConsumer {

    protected static final String OCR_REQUEST_TOPICS = "ocr-request";
    protected static final String OCR_REQUEST_RETRY_TOPICS = "ocr-request-retry";
    protected static final String OCR_REQUEST_ERROR_TOPICS = "ocr-request-error";

    private static final String OCR_REQUEST_KEY_RETRY = OCR_REQUEST_RETRY_TOPICS;

    private static final Logger LOG = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private static final String OCR_REQUEST_GROUP = APPLICATION_NAME_SPACE + "-" + OCR_REQUEST_TOPICS;
    private static final String OCR_REQUEST_RETRY_GROUP = APPLICATION_NAME_SPACE + "-" + OCR_REQUEST_RETRY_TOPICS;
    private static final String OCR_REQUEST_ERROR_GROUP = APPLICATION_NAME_SPACE + "-" + OCR_REQUEST_ERROR_TOPICS;

    private static final String KAFKA_LISTENER_CONTAINER_FACTORY = "kafkaListenerContainerFactory";

    private static final int MAX_RETRY_ATTEMPTS = 3;

    private static long errorRecoveryOffset = 0L;
 
    private final Map<String, Integer> retryCount;

    private final SerializerFactory serializerFactory;
    private final OcrApiConsumerKafkaProducer kafkaProducer;
    private final KafkaListenerEndpointRegistry registry;
    private final OcrApiConsumerService ocrApiConsumerService;

    @Autowired
    public OcrApiConsumerKafkaConsumer(SerializerFactory serializerFactory, OcrApiConsumerKafkaProducer kafkaProducer, final OcrApiConsumerService ocrApiConsumerService, KafkaListenerEndpointRegistry registry) {

        this.retryCount = new ConcurrentHashMap<>();
        this.serializerFactory = serializerFactory;
        this.kafkaProducer = kafkaProducer;
        this.registry = registry;
        this.ocrApiConsumerService = ocrApiConsumerService;
    }

    /**
     * Consumes from the 'Main' Kafka Topic.
     *
     * @param message - processed via the common `handleMessage` method.
     */
    @KafkaListener(
        id = OCR_REQUEST_GROUP,
        topics = OCR_REQUEST_TOPICS,
        groupId = OCR_REQUEST_GROUP,
        autoStartup = "#{!${uk.gov.companieshouse.ocrapiconsumer.error-consumer}}",
        containerFactory = KAFKA_LISTENER_CONTAINER_FACTORY)
    public void processOcrApiRequest(org.springframework.messaging.Message<OcrRequestMessage> message) {    
        handleMessage(message);
    }

    /**
     * Consumes from the 'Retry' Kafka Topic.
     *
     * @param message - processed via the common `handleMessage` method.
     */
    @KafkaListener(
        id = OCR_REQUEST_RETRY_GROUP,
        topics = OCR_REQUEST_RETRY_TOPICS,
        groupId = OCR_REQUEST_RETRY_GROUP,
        autoStartup = "#{!${uk.gov.companieshouse.ocrapiconsumer.error-consumer}}",
        containerFactory = KAFKA_LISTENER_CONTAINER_FACTORY)
    public void retryOcrApiRequest(org.springframework.messaging.Message<OcrRequestMessage> message) {
        handleMessage(message);
    }

    /**
     * Consumes from the 'Error' Kafka Topic.
     *
     * The Error (`-error`) topic listener/consumer is enabled when the application is launched in error
     * mode (IS_ERROR_QUEUE_CONSUMER=true). 
     * 
     * This receives messages up to `errorRecoveryOffset` offset.
     * 
     * Calls the common `handleMessage` method to process received message. If the `retryable` processor is
     * unsuccessful with a `retryable` error, after maximum numbers of attempts allowed, the message
     * is republished to `-retry` topic for failover processing. This listener stops accepting
     * messages when the topic's offset reaches `errorRecoveryOffset`.
     *
     * @param message
     */
    @KafkaListener(
        id = OCR_REQUEST_ERROR_GROUP,
        topics = OCR_REQUEST_ERROR_TOPICS,
        groupId = OCR_REQUEST_ERROR_GROUP,
        autoStartup = "#{!${uk.gov.companieshouse.ocrapiconsumer.error-consumer}}",
        containerFactory = KAFKA_LISTENER_CONTAINER_FACTORY)
    public void errorOcrApiRequest(org.springframework.messaging.Message<OcrRequestMessage> message) {

        long offset = Long.parseLong("" + message.getHeaders().get("kafka_offset"));

        if (offset <= errorRecoveryOffset) {
            handleMessage(message);
        } else {
            Map<String, Object> logMap = LoggingUtils.createLogMap();
            logMap.put(LoggingUtils.OCR_REQUEST_ERROR_GROUP, errorRecoveryOffset);
            logMap.put(LoggingUtils.TOPIC, OCR_REQUEST_ERROR_TOPICS);
            LOG.info("Pausing error consumer as error recovery offset reached.", logMap);
            
            registry.getListenerContainer(OCR_REQUEST_ERROR_GROUP).pause();
        }
    }

    private void handleMessage(org.springframework.messaging.Message<OcrRequestMessage> message) {

        final OcrRequestMessage requestMessage = message.getPayload();
        final MessageHeaders headers = message.getHeaders();
        String receivedTopic = "";
        String contextId = message.getPayload().getResponseId();

        try {
            receivedTopic = extractedReceivedTopicName(headers);
           
            LOG.infoContext(contextId, receivedTopic + " Orchestrate OCR Request for Kafka Message", null);

            ocrApiConsumerService.ocrRequest(requestMessage);

            logMessageProcessed(message, requestMessage);

            String counterKey = OCR_REQUEST_TOPICS + "-" + contextId;
            if (getRetryCount().containsKey(counterKey)) {
                // housekeep map now we have successfully processed this message
                resetRetryCount(counterKey);
            }
        } catch (RetryableErrorException ex) {
            retryMessage(message, requestMessage, receivedTopic, OCR_REQUEST_TOPICS,ex);
        } catch (DuplicateErrorException dx) {
            logMessageProcessingFailureDuplicateItem(contextId, message, dx);
        } catch (Exception x) {
            /**
             * TODO - IVP-1251
             */
            logMessageProcessingFailureNonRecoverable(contextId, message, x);
            throw x;
        }
    }

    private String extractedReceivedTopicName(final MessageHeaders headers) {
       
        String headerKey = KafkaHeaders.RECEIVED_TOPIC;
        Object topicNameObject = headers.get(headerKey);

        if (topicNameObject != null) {
            return topicNameObject.toString();
        }
        else {
            throw new FatalErrorException("Missing key [" + headerKey + "] in KafkaHeaders");
        }
    }

    private void logMessageProcessed(org.springframework.messaging.Message<OcrRequestMessage> message, OcrRequestMessage ocrRequestMessage) {
        Map<String, Object> logMap = LoggingUtils.getMessageHeadersAsMap(message);
        LOG.infoContext(ocrRequestMessage.getResponseId(), "'ocr-request' message processing completed ", logMap);
    }

    private void logMessageProcessingFailureDuplicateItem(
        String contextId,
        org.springframework.messaging.Message<OcrRequestMessage> message, 
        Exception exception) {

        Map<String, Object> logMap = LoggingUtils.getMessageHeadersAsMap(message);
        LOG.errorContext(contextId, "Message processing failed item already exists", exception, logMap);
    }

    protected void logMessageProcessingFailureNonRecoverable(
        String contextId,
        org.springframework.messaging.Message<OcrRequestMessage> message, 
        Exception exception) {

        Map<String, Object> logMap = LoggingUtils.getMessageHeadersAsMap(message);
        LOG.errorContext(contextId, "Message processing failed with a  non-recoverable (fatal) exception",
                exception, logMap);
    }

    protected void logMessageProcessingFailureRecoverable(
        String contextId,
        org.springframework.messaging.Message<OcrRequestMessage> message, 
        int attempt,
        Exception exception) {

        Map<String, Object> logMap = LoggingUtils.getMessageHeadersAsMap(message);
        logMap.put(LoggingUtils.RETRY_ATTEMPT, attempt);
        LOG.errorContext(contextId, "Message processing failed with a recoverable exception ", exception, logMap);
}

    /**
     * Resets retryCount for message identified by key `counterKey`
     *
     * @param counterKey
     */
    private void resetRetryCount(String counterKey) {
        retryCount.remove(counterKey);
    }

    /**
     * Retries a message that failed processing with a `RetryableErrorException`.
     * Checks which topic the message was received from and whether any retry
     * attempts remain. The message is published to the next topic for failover
     * processing, if retries match or exceed `MAX_RETRY_ATTEMPTS`.
     *
     * @param message
     * @param ocrRequestMessage
     * @param receivedTopic
     * @param counterKeyPrefix
     * @param ocrRequestTopics
     * @param ex
     */
    private void retryMessage(
        org.springframework.messaging.Message<OcrRequestMessage> message,
        final OcrRequestMessage ocrRequestMessage, 
        String receivedTopic, 
        String counterKeyPrefix,
        RetryableErrorException ex) {

        String nextTopic = (receivedTopic.equals(OCR_REQUEST_TOPICS) ||
            receivedTopic.equals(OCR_REQUEST_ERROR_TOPICS)) ?
                OCR_REQUEST_RETRY_TOPICS :
                OCR_REQUEST_ERROR_TOPICS;

        String counterKey = counterKeyPrefix + "-" + ocrRequestMessage.getResponseId();

        if (receivedTopic.equals(OCR_REQUEST_TOPICS) || 
            retryCount.getOrDefault(counterKey, 1) >= MAX_RETRY_ATTEMPTS) {

            republishMessageToTopic(ocrRequestMessage, receivedTopic, nextTopic);

            if (!receivedTopic.equals(OCR_REQUEST_TOPICS)) {
                // publishing now to the error topic
                resetRetryCount(counterKey);
            }
            else {
                retryCount.put(counterKey, 0);
            }
        } else {
            // Retrying sending the message on the retry count
            // TODO - IVP-1285
            retryCount.put(counterKey, retryCount.getOrDefault(counterKey, 0) + 1);

            logMessageProcessingFailureRecoverable(ocrRequestMessage.getResponseId(), message, retryCount.get(counterKey), ex);

            // retry
            handleMessage(message);
        }
    }

    protected void republishMessageToTopic(final OcrRequestMessage ocrRequestMessage,
            final String currentTopic,
            final String nextTopic) {
        
        Map<String, Object> logMap = LoggingUtils.createLogMap();
        logIfNotNull(logMap, LoggingUtils.MESSAGE, ocrRequestMessage.getResponseId());
        logIfNotNull(logMap, LoggingUtils.CURRENT_TOPIC, currentTopic);
        logIfNotNull(logMap, LoggingUtils.NEXT_TOPIC, nextTopic);

        LOG.infoContext(ocrRequestMessage.getResponseId(), String.format(
            "Republishing message: \"%1$s\" received from topic: \"%2$s\" to topic: \"%3$s\"",
            ocrRequestMessage.getResponseId(), currentTopic, nextTopic), null);

        try {
            kafkaProducer.sendMessage(createRetryMessage(ocrRequestMessage, nextTopic));

        } catch (ExecutionException | InterruptedException exception) {
            
            LOG.errorContext(ocrRequestMessage.getResponseId(), String.format("Error sending message: \"%1$s\" to topic: \"%2$s\"",
                ocrRequestMessage.getResponseId(), nextTopic), exception, null);

            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    protected Message createRetryMessage(final OcrRequestMessage ocrRequestMessage,
                                         final String topic) {

        final Message message = new Message();
        final AvroSerializer<OcrRequestMessage> serializer =
                serializerFactory.getGenericRecordSerializer(OcrRequestMessage.class);
        message.setKey(OCR_REQUEST_KEY_RETRY);

        try {
            message.setValue(serializer.toBinary(ocrRequestMessage));
        } catch (SerializationException e) {
            Map<String, Object> logMap = LoggingUtils.createLogMap();
            logIfNotNull(logMap, LoggingUtils.MESSAGE, ocrRequestMessage.getResponseId());
            logIfNotNull(logMap, LoggingUtils.TOPIC, topic);
            logIfNotNull(logMap, LoggingUtils.OFFSET, message.getOffset());
            LOG.errorContext(ocrRequestMessage.getResponseId(),String.format("Error serializing message: \"%1$s\" for topic: \"%2$s\"",
                ocrRequestMessage.getResponseId(), topic), e, logMap);
        }

        message.setTopic(topic);
        message.setTimestamp(new Date().getTime());

        return message;
    }

    // Use for unit testing
    protected  Map<String, Integer> getRetryCount() {
        return  retryCount;
    }

}
