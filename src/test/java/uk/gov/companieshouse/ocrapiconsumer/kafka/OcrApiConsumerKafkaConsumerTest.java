package uk.gov.companieshouse.ocrapiconsumer.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.time.StopWatch;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.listener.adapter.ConsumerRecordMetadata;
import org.springframework.messaging.MessageHeaders;

import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;
import uk.gov.companieshouse.ocr.OcrRequestMessage;
import uk.gov.companieshouse.ocrapiconsumer.groups.Unit;
import uk.gov.companieshouse.ocrapiconsumer.kafka.exception.FatalErrorException;
import uk.gov.companieshouse.ocrapiconsumer.kafka.exception.RetryableErrorException;
import uk.gov.companieshouse.ocrapiconsumer.request.OcrApiConsumerService;

@Unit
@ExtendWith(MockitoExtension.class)
class OcrApiConsumerKafkaConsumerTest {

    private static final String CONTEXT_ID = "1";

    private static final long RETRY_THROTTLE_RATE_SECONDS = 3L;

    @Mock
    private SerializerFactory serializerFactory;
    @Mock
    private AvroSerializer serializer;
    @Mock
    private OcrApiConsumerKafkaProducer kafkaProducer;
    @Mock
    private OcrApiConsumerService ocrApiConsumerService;
    @Mock
    private OcrMessageErrorHandler ocrMessageErrorHandler;

    @InjectMocks
    private OcrApiConsumerKafkaConsumer kafkaConsumer;

    @BeforeEach
    public void setup() {
        this.kafkaConsumer = new OcrApiConsumerKafkaConsumer(serializerFactory, kafkaProducer, ocrApiConsumerService, ocrMessageErrorHandler);
        kafkaConsumer.retryThrottleRateSeconds = RETRY_THROTTLE_RATE_SECONDS;
    }

    @Test
    @DisplayName("Successfully handle a message published on the Main ocr-request")
    void shouldProcessOcrApiRequest() {

        // Given
        org.springframework.messaging.Message<OcrRequestMessage> message = createTestMessage(kafkaConsumer.getMainTopicName());

        // When
        kafkaConsumer.consumeOcrApiRequestMessage(message, metadataWithTopic(kafkaConsumer.getMainTopicName()));

        // Then
        verify(ocrApiConsumerService).ocrRequest(message.getPayload());
    }


    // Test that we re-try a message when we get a RetryableErrorException
    @Test
    @DisplayName("Add message to retry topic when we get a retryable error on the main topic")
    void sentMessageToRetryTopicAfterRetryableErrorOnMainTopic() throws SerializationException,
            ExecutionException, InterruptedException {

        // Given
        org.springframework.messaging.Message<OcrRequestMessage> message = createTestMessage(kafkaConsumer.getMainTopicName());
        doThrow(newRetryableError())
				.when(ocrApiConsumerService).ocrRequest(message.getPayload());
        when(serializerFactory.getGenericRecordSerializer(OcrRequestMessage.class)).thenReturn(serializer);
        when(serializer.toBinary(any())).thenReturn(new byte[4]);

        StopWatch watch = new StopWatch();
        watch.start();

        // When
        kafkaConsumer.consumeOcrApiRequestMessage(message, metadataWithTopic(kafkaConsumer.getMainTopicName()));

        watch.stop();

        assertTrue(watch.getTime() > (RETRY_THROTTLE_RATE_SECONDS * 1000));

        // Then
        verify(kafkaProducer).sendMessage(any());
    }

    @Test
    @DisplayName("Successfully handle a message published on the Retry ocr-request")
    void shouldRetryOcrApiRequest() {

        // Given
        org.springframework.messaging.Message<OcrRequestMessage> message = createTestMessage(kafkaConsumer.getRetryTopicName());

        // When
        kafkaConsumer.consumeOcrApiRequestRetryMessage(message, metadataWithTopic(kafkaConsumer.getRetryTopicName()));

        // Then
        verify(ocrApiConsumerService).ocrRequest(message.getPayload());
    }

    // Test that we re-try a message when we get a RetryableErrorException
    @Test
    @DisplayName("A retryable error occurs on a message from the retry Topic but then successed before max count is set")
    void retryableErrorOnceNotMaxOnRetryTopic()
            throws SerializationException, ExecutionException, InterruptedException {

        // Given
        org.springframework.messaging.Message<OcrRequestMessage> message = createTestMessage(kafkaConsumer.getRetryTopicName());
        String expectedCounterKey = CONTEXT_ID;

        doThrow(newRetryableError()).doNothing()
                .when(ocrApiConsumerService).ocrRequest(message.getPayload());

        StopWatch watch = new StopWatch();
        watch.start();

        // When
        kafkaConsumer.consumeOcrApiRequestRetryMessage(message, metadataWithTopic(kafkaConsumer.getRetryTopicName()));

        watch.stop();

        assertTrue(watch.getTime() > (RETRY_THROTTLE_RATE_SECONDS * 1000));

        // Then
        verify(kafkaProducer, never()).sendMessage(any());
        assertEquals(null, kafkaConsumer.getRetryCounts().get(expectedCounterKey),
                "retry count reset after ocr service works");

    }

    @Test
    @DisplayName("A retryable error occurs on a message from the retry Topic and reaches max retries and logs error")
    void retryableErrorRetryAboveMaxRetriesTopic()
            throws SerializationException, ExecutionException, InterruptedException {

        // Given
        org.springframework.messaging.Message<OcrRequestMessage> message = createTestMessage(
                kafkaConsumer.getRetryTopicName());
        String expectedCounterKey = CONTEXT_ID;

        doThrow(newRetryableError()).doThrow(newRetryableError()).doThrow(newRetryableError())
                .when(ocrApiConsumerService).ocrRequest(message.getPayload());

        StopWatch watch = new StopWatch();
        watch.start();

        // When
        kafkaConsumer.consumeOcrApiRequestRetryMessage(message, metadataWithTopic(kafkaConsumer.getRetryTopicName()));

        watch.stop();

        assertTrue(watch.getTime() > (RETRY_THROTTLE_RATE_SECONDS * 1000));

        // Then
        verify(kafkaProducer, never()).sendMessage(any());
        verify(ocrMessageErrorHandler).handleMaximumRetriesException(any(), any());
        assertNull(kafkaConsumer.getRetryCounts().get(expectedCounterKey), "retry count reset sending error message");
    }

    @Test
    @DisplayName("A retryable error occurs on a message and then any exception except MaximumRetriesException")
    void GeneralException()
            throws SerializationException, ExecutionException, InterruptedException {

        // Given
        org.springframework.messaging.Message<OcrRequestMessage> message = createTestMessage(
                kafkaConsumer.getRetryTopicName());
        String expectedCounterKey = CONTEXT_ID;

        doThrow(newRetryableError()).doThrow(newFatalError())
                .when(ocrApiConsumerService).ocrRequest(message.getPayload());

        // When
        kafkaConsumer.consumeOcrApiRequestRetryMessage(message, metadataWithTopic(kafkaConsumer.getRetryTopicName()));

        // Then
        verify(kafkaProducer, never()).sendMessage(any());
        verify(ocrMessageErrorHandler).generalException(any(), any());
        assertNull(kafkaConsumer.getRetryCounts().get(expectedCounterKey), "retry count reset sending error message");
    }

    private RetryableErrorException newRetryableError() {
        return new RetryableErrorException("Dummy", new Exception("dummy cause"));
    }

    private FatalErrorException newFatalError() {
        return new FatalErrorException("Dummy");
    }

    private org.springframework.messaging.Message<OcrRequestMessage> createTestMessage(String receivedTopic) {
        return new org.springframework.messaging.Message<OcrRequestMessage>() {

            @Override
            public OcrRequestMessage getPayload() {
                return createOcrRequest();
            }

            private OcrRequestMessage createOcrRequest() {

                OcrRequestMessage ocrRequestMessage = new OcrRequestMessage();
                ocrRequestMessage.setResponseId(CONTEXT_ID);

                return ocrRequestMessage;
            }

            @Override
            public MessageHeaders getHeaders() {

                Map<String, Object> headerItems = new HashMap<>();

                headerItems.put("kafka_receivedTopic", receivedTopic);
                headerItems.put("kafka_offset", 0);
                headerItems.put("kafka_receivedMessageKey", CONTEXT_ID);
                headerItems.put("kafka_receivedPartitionId", 0);
                
                return new MessageHeaders(headerItems);
            }
        };
    }

    private ConsumerRecordMetadata metadataWithTopic(String topicName) {

        TopicPartition topicPartition = new TopicPartition(topicName, 1);  
        return new ConsumerRecordMetadata(new RecordMetadata(topicPartition, 0,0,0,0L,0, 0), null);
    }

}
