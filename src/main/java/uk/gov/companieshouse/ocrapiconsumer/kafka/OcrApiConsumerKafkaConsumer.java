package uk.gov.companieshouse.ocrapiconsumer.kafka;

import static uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication.APPLICATION_NAME_SPACE;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.adapter.ConsumerRecordMetadata;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocr.OcrRequestMessage;
import uk.gov.companieshouse.ocrapiconsumer.common.EnvironmentVariable;
import uk.gov.companieshouse.ocrapiconsumer.kafka.exception.FatalErrorException;
import uk.gov.companieshouse.ocrapiconsumer.kafka.exception.MaximumRetriesException;
import uk.gov.companieshouse.ocrapiconsumer.kafka.exception.RetryableErrorException;
import uk.gov.companieshouse.ocrapiconsumer.request.OcrApiConsumerService;

@Service
public class OcrApiConsumerKafkaConsumer {

    protected static final String OCR_REQUEST_TOPICS = "ocr-request";
    protected static final String OCR_REQUEST_RETRY_TOPICS = "ocr-request-retry";

    private static final Logger LOG = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private static final String OCR_REQUEST_GROUP = APPLICATION_NAME_SPACE + "-" + OCR_REQUEST_TOPICS;
    private static final String OCR_REQUEST_RETRY_GROUP = APPLICATION_NAME_SPACE + "-" + OCR_REQUEST_RETRY_TOPICS;

    private static final String KAFKA_LISTENER_CONTAINER_FACTORY = "kafkaListenerContainerFactory";

    private static final int MAX_RETRY_ATTEMPTS = 3;

    private final OcrApiConsumerService ocrApiConsumerService;
    private final OcrMessageErrorHandler ocrMessageErrorHandler;
    private final SerializerFactory serializerFactory;
    private final OcrApiConsumerKafkaProducer kafkaProducer;
    private final EnvironmentReader environmentReader;

    protected long retryThrottleRateSeconds;

    @Autowired
    public OcrApiConsumerKafkaConsumer(SerializerFactory serializerFactory,
                                       OcrApiConsumerKafkaProducer kafkaProducer,
                                       final OcrApiConsumerService ocrApiConsumerService,
                                       OcrMessageErrorHandler ocrMessageErrorHandler,
                                       final EnvironmentReader environmentReader) {

        this.serializerFactory = serializerFactory;
        this.kafkaProducer = kafkaProducer;
        this.ocrApiConsumerService = ocrApiConsumerService;
        this.ocrMessageErrorHandler = ocrMessageErrorHandler;
        this.environmentReader = environmentReader;
    }

    @KafkaListener(
        id = OCR_REQUEST_GROUP,
        topics = OCR_REQUEST_TOPICS,
        groupId = OCR_REQUEST_GROUP,
        concurrency ="${kafka.consumer.main.topic.concurrency}",
        containerFactory = KAFKA_LISTENER_CONTAINER_FACTORY)
    public void consumeOcrApiRequestMessage(org.springframework.messaging.Message<OcrRequestMessage> message, ConsumerRecordMetadata metadata) {

        logConsumeKafkaMessage(message.getPayload(), metadata);

        handleOcrRequestMessage(message, metadata.topic());
    }

    @KafkaListener(
        id = OCR_REQUEST_RETRY_GROUP,
        topics = OCR_REQUEST_RETRY_TOPICS,
        groupId = OCR_REQUEST_RETRY_GROUP,
        concurrency ="${kafka.consumer.retry.topic.concurrency}",
        containerFactory = KAFKA_LISTENER_CONTAINER_FACTORY)
    public void consumeOcrApiRequestRetryMessage(org.springframework.messaging.Message<OcrRequestMessage> message, ConsumerRecordMetadata metadata) {
        
        logConsumeKafkaMessage(message.getPayload(), metadata);

        delayRetry(message.getPayload().getContextId());

        handleOcrRequestMessage(message, metadata.topic());
    }

    private void handleOcrRequestMessage(org.springframework.messaging.Message<OcrRequestMessage> message,
            String topicName) {

        OcrRequestMessage ocrRequestMessage = message.getPayload();
        String contextId = ocrRequestMessage.getContextId();
        String responseId = ocrRequestMessage.getResponseId();
        String extractedTextEndpoint = ocrRequestMessage.getConvertedTextEndpoint();

        try {

            ocrApiConsumerService.ocrRequest(ocrRequestMessage);

        } catch (RetryableErrorException ree) {

            LOG.errorContext(contextId, "Retryable Error consuming message", ree, null);
            
            try {
                retryMessage(message, topicName);

            } catch (MaximumRetriesException mre) {
                ocrMessageErrorHandler
                        .handleMaximumRetriesException(contextId, responseId, mre, extractedTextEndpoint);

            } catch (Exception ex) {
                ocrMessageErrorHandler.generalExceptionAfterRetry(contextId, responseId, ex, extractedTextEndpoint);
            }
        } catch (Exception exception) {
            ocrMessageErrorHandler.generalException(contextId, responseId, exception, extractedTextEndpoint);

        }
    }

    @SuppressWarnings("java:S2142")
    private void delayRetry(String contextId) {
        retryThrottleRateSeconds = environmentReader
                .getMandatoryLong(EnvironmentVariable.RETRY_THROTTLE_RATE_SECONDS.name());

        LOG.infoContext(contextId, "Pausing thread [" + retryThrottleRateSeconds + "] seconds before retrying",  null);

        try {
            TimeUnit.SECONDS.sleep(retryThrottleRateSeconds);
        } catch (InterruptedException e) {
            // We want to continue processing even if somehow our delay was cut short
            LOG.errorContext(contextId, "Error pausing thread", e, null);
        }
    }

    private void retryMessage(org.springframework.messaging.Message<OcrRequestMessage> message, String currentTopic) {

        OcrRequestMessage ocrRequestMessage = message.getPayload();
        Integer nextAttempt = ocrRequestMessage.getAttempt() + 1;

        if (nextAttempt > getMaxRetryAttempts()) {

            throw new MaximumRetriesException();
        }
        else {
            ocrRequestMessage.setAttempt(nextAttempt);
            repostMessage(ocrRequestMessage, currentTopic, getRetryTopicName());
        }
    }

    private void repostMessage(final OcrRequestMessage ocrRequestMessage, final String fromTopic, final String toTopic) {

        Message retryMessage = createRepostMessage(ocrRequestMessage, toTopic);
        String contextId = ocrRequestMessage.getContextId();

        LOG.infoContext(contextId, "Reposting message from topic [" + fromTopic + "]" + " to topic [" + toTopic + "]", null);

        String failureMessage = "Can not repost message";
        try {
            kafkaProducer.sendMessage(retryMessage);

        } catch (InterruptedException ie) {

            Thread.currentThread().interrupt();

            throw new FatalErrorException(failureMessage, ie);

        }  catch (ExecutionException ee) {

            throw new FatalErrorException(failureMessage, ee);
        }
    }

    private Message createRepostMessage(final OcrRequestMessage ocrRequestMessage,
            final String topic) {

        Message retryMessage = new Message();
        AvroSerializer<OcrRequestMessage> serializer = serializerFactory.getGenericRecordSerializer(OcrRequestMessage.class);

        try {
            retryMessage.setValue(serializer.toBinary(ocrRequestMessage));
        } catch (SerializationException exception) {
            throw new FatalErrorException("Can not serialise ocr-request message", exception);
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

    protected int getMaxRetryAttempts() {
        return MAX_RETRY_ATTEMPTS;
    }

    // logging helper methods
    private void logConsumeKafkaMessage(OcrRequestMessage ocrRequestMessage, ConsumerRecordMetadata metadata) {

        Map<String, Object> metadataMap = new LinkedHashMap<>();
        metadataMap.put("topic", metadata.topic());
        metadataMap.put("partition", metadata.partition());
        metadataMap.put("offset", metadata.offset());
        metadataMap.put("thread_id", Long.valueOf(Thread.currentThread().getId()));
        metadataMap.put("attempt_so_far", ocrRequestMessage.getAttempt());
        metadataMap.put("created_at", ocrRequestMessage.getCreatedAt());

        LOG.infoContext(ocrRequestMessage.getContextId(), "Consuming ocr-request Message ", metadataMap);
    }

}
