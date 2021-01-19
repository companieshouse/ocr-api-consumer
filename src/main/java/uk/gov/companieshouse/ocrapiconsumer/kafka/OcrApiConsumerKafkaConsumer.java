package uk.gov.companieshouse.ocrapiconsumer.kafka;

import static uk.gov.companieshouse.ocrapiconsumer.logging.LoggingUtils.logIfNotNull;
import static uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication.APPLICATION_NAME_SPACE;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;

import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;

import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocr.OcrRequestMessage;
import uk.gov.companieshouse.ocrapiconsumer.exception.RetryableErrorException;
import uk.gov.companieshouse.ocrapiconsumer.logging.LoggingUtils;
import uk.gov.companieshouse.ocrapiconsumer.request.OcrApiConsumerService;

@Service
public class OcrApiConsumerKafkaConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private static final String OCR_REQUEST_TOPICS = "ocr-request";
    private static final String OCR_REQUEST_RETRY_TOPICS = "ocr-request-retry";
    private static final String OCR_REQUEST_KEY_RETRY = OCR_REQUEST_RETRY_TOPICS;
    private static final String OCR_REQUEST_ERROR_TOPICS = "ocr-request-error";
    private static final String OCR_REQUEST_GROUP = APPLICATION_NAME_SPACE + "-" + OCR_REQUEST_TOPICS;
    private static final String OCR_REQUEST_RETRY_GROUP = APPLICATION_NAME_SPACE + "-" + OCR_REQUEST_RETRY_TOPICS;
    private static final String OCR_REQUEST_ERROR_GROUP = APPLICATION_NAME_SPACE + "-" + OCR_REQUEST_ERROR_TOPICS;

    private static final int MAX_RETRY_ATTEMPTS = 3;

    private static long errorRecoveryOffset = 0L;

    private final Map<String, Integer> retryCount;

    private final SerializerFactory serializerFactory;
    private final OcrApiConsumerKafkaProducer kafkaProducer;
    private final KafkaListenerEndpointRegistry registry;
    private final OcrApiConsumerService ocrApiConsumerService;

    @Autowired
    public OcrApiConsumerKafkaConsumer(SerializerFactory serializerFactory, OcrApiConsumerKafkaProducer kafkaProducer, final OcrApiConsumerService ocrApiConsumerService, KafkaListenerEndpointRegistry registry) {
        this.retryCount = new HashMap<>();
        this.serializerFactory = serializerFactory;
        this.kafkaProducer = kafkaProducer;
        this.ocrApiConsumerService = ocrApiConsumerService;
        this.registry = registry;
    }

    @KafkaListener(
        id = OCR_REQUEST_GROUP,
        topics = OCR_REQUEST_TOPICS,
        groupId = OCR_REQUEST_GROUP,
        autoStartup = "#{!${uk.gov.companieshouse.ocrapiconsumer.error-consumer}}",
        containerFactory = "kafkaListenerContainerFactory")
    public void processOcrApiRequest(org.springframework.messaging.Message<OcrRequestMessage> message) {
        
        LOG.debug("Received Messasge responseId: " + message.getPayload().getResponseId());
        handleMessage(message);
    }

    @KafkaListener(
        id = OCR_REQUEST_RETRY_GROUP,
        topics = OCR_REQUEST_RETRY_TOPICS,
        groupId = OCR_REQUEST_RETRY_GROUP,
        autoStartup = "#{!${uk.gov.companieshouse.ocrapiconsumer.error-consumer}}",
        containerFactory = "kafkaListenerContainerFactory")
    public void retryOcrApiRequest(org.springframework.messaging.Message<OcrRequestMessage> message) {
        handleMessage(message);
    }

    @KafkaListener(
        id = OCR_REQUEST_ERROR_GROUP,
        topics = OCR_REQUEST_ERROR_TOPICS,
        groupId = OCR_REQUEST_ERROR_GROUP,
        autoStartup = "#{!${uk.gov.companieshouse.ocrapiconsumer.error-consumer}}",
        containerFactory = "kafkaListenerContainerFactory")
    public void errorOcrApiRequest(org.springframework.messaging.Message<OcrRequestMessage> message) {

        long offset = Long.parseLong("" + message.getHeaders().get("kafka_offset"));

        if (offset <= errorRecoveryOffset) {
            handleMessage(message);
        } else {
            // Map<String, Object> logMap = LoggingUtils.createLogMap();
            // logMap.put(LoggingUtils.CHD_ITEM_ORDERED_GROUP_ERROR, errorRecoveryOffset);
            // logMap.put(LoggingUtils.TOPIC, CHD_ITEM_ORDERED_TOPIC_ERROR);
            LOG.info("Pausing error consumer as error recovery offset reached."); // todo: add additional info from above
            
            registry.getListenerContainer(OCR_REQUEST_ERROR_GROUP).pause();
        }
    }

    private void handleMessage(org.springframework.messaging.Message<OcrRequestMessage> message) {

        final OcrRequestMessage requestMessage = message.getPayload();
        final String responseId = requestMessage.getResponseId();
        final MessageHeaders headers = message.getHeaders();
        final String receivedTopic = headers.get(KafkaHeaders.RECEIVED_TOPIC).toString();

        try {
            // log message received

            // process message
            // ocrApiConsumerService.sendOcrApiRequest(responseId);
            if (retryCount.containsKey(responseId)) {
                resetRetryCount(receivedTopic + "-" + responseId);
            }

            logMessageProcessed(message, requestMessage);

        // } catch (RetryableErrorException ex) {
        //     retryMessage(message, order, orderReference, receivedTopic, ex);
        // } catch (DuplicateErrorException dx) {
        //     logMessageProcessingFailureDuplicateItem(message, dx);
        } catch (Exception x) {
            // logMessageProcessingFailureNonRecoverable(message, x);
            throw x;
        }
    }

    private void logMessageProcessed(org.springframework.messaging.Message<OcrRequestMessage> message, OcrRequestMessage ocrRequestMessage) {

        LOG.infoContext(message.getPayload().getResponseId(), "'ocr-request' message processing completed ", null);
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
     * Retries a message that failed processing with a `RetryableErrorException`. Checks which topic
     * the message was received from and whether any retry attempts remain. The message is published
     * to the next topic for failover processing, if retries match or exceed `MAX_RETRY_ATTEMPTS`.
     *
     * @param message
     * @param ocrRequestMessage
     * @param orderReference
     * @param receivedTopic
     * @param ex
     */
    private void retryMessage(org.springframework.messaging.Message<OcrRequestMessage> message,
            final OcrRequestMessage ocrRequestMessage,
            String orderReference, String receivedTopic, RetryableErrorException ex) {

        String nextTopic = (receivedTopic.equals(OCR_REQUEST_TOPICS) ||
            receivedTopic.equals(OCR_REQUEST_ERROR_TOPICS)) ?
                OCR_REQUEST_RETRY_TOPICS :
                OCR_REQUEST_ERROR_TOPICS;

        String counterKey = receivedTopic + "-" + orderReference;

        if (receivedTopic.equals(OCR_REQUEST_TOPICS) || retryCount.getOrDefault(counterKey, 1) >= MAX_RETRY_ATTEMPTS) {

            republishMessageToTopic(ocrRequestMessage, orderReference, receivedTopic, nextTopic);

            if (!receivedTopic.equals(OCR_REQUEST_TOPICS)) {
                resetRetryCount(counterKey);
            }
        } else {
            retryCount.put(counterKey, retryCount.getOrDefault(counterKey, 1) + 1);

            logMessageProcessingFailureRecoverable(message, retryCount.get(counterKey), ex);

            // retry
            handleMessage(message);
        }
    }

    protected void republishMessageToTopic(final OcrRequestMessage ocrRequestMessage,
            final String orderReference,
            final String currentTopic,
            final String nextTopic) {
        
        // Map<String, Object> logMap = LoggingUtils.createLogMap();
        // logIfNotNull(logMap, ORDER_REFERENCE_NUMBER, orderReference);
        // logIfNotNull(logMap, LoggingUtils.CURRENT_TOPIC, currentTopic);
        // logIfNotNull(logMap, LoggingUtils.NEXT_TOPIC, nextTopic);

        LOG.info(String.format(
            "Republishing message: \"%1$s\" received from topic: \"%2$s\" to topic: \"%3$s\"",
            orderReference, currentTopic, nextTopic));

        try {
            kafkaProducer.sendMessage(createRetryMessage(ocrRequestMessage, nextTopic));

        } catch (ExecutionException | InterruptedException exception) {
            
            LOG.error(String.format("Error sending message: \"%1$s\" to topic: \"%2$s\"",
                    orderReference, nextTopic), exception);

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
            LOG.error(String.format("Error serializing message: \"%1$s\" for topic: \"%2$s\"",
            ocrRequestMessage.getResponseId(), topic), e, logMap);
        }
        message.setTopic(topic);
        message.setTimestamp(new Date().getTime());

        return message;
    }


    protected void logMessageProcessingFailureRecoverable(
            org.springframework.messaging.Message<OcrRequestMessage> message, int attempt,
            Exception exception) {
        Map<String, Object> logMap = LoggingUtils.getMessageHeadersAsMap(message);
        logMap.put(LoggingUtils.RETRY_ATTEMPT, attempt);
        LOG.error("'ocr-request' message processing failed with a recoverable exception",
                exception, logMap);
    }

}
