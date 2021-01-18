package uk.gov.companieshouse.ocrapiconsumer.kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocr.OcrRequestMessage;
import uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication;
import uk.gov.companieshouse.ocrapiconsumer.exception.RetryableErrorException;
import uk.gov.companieshouse.ocrapiconsumer.request.OcrApiConsumerService;
import uk.gov.companieshouse.ocrapiconsumer.request.OcrRequest;

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

    private final Map<String, Integer> retryCount;

    private final KafkaListenerEndpointRegistry registry;
    private final OcrApiConsumerService ocrApiConsumerService;

    @Autowired
    public OcrApiConsumerConsumer(final OcrApiConsumerService ocrApiConsumerService, KafkaListenerEndpointRegistry registry) {
        this.retryCount = new HashMap<>();
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
        
        // LOG.debug("Received Messasge in group - group-id: " + message.getPayload().getResponseId());

        // OcrRequestMessage payload = message.getPayload();

        // LOG.debug(String.format("responseId: %s, appId: %s, attempt: %s, imageEndpoint: %s, convertedTextEndpoint: %s, createdAt: %s", payload.getResponseId(), payload.getAppId(), payload.getAttempt(), payload.getImageEndpoint(), payload.getConvertedTextEndpoint(), payload.getCreatedAt()));
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
            kafkaProducer.sendMessage(createRetryMessage(ocrRequestMessage, orderReference, nextTopic));

        } catch (ExecutionException | InterruptedException exception) {
            
            LOG.error(String.format("Error sending message: \"%1$s\" to topic: \"%2$s\"",
                    orderReference, nextTopic), exception);

            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
