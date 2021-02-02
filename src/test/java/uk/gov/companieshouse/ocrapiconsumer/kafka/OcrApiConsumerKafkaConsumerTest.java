package uk.gov.companieshouse.ocrapiconsumer.kafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.ocrapiconsumer.kafka.OcrApiConsumerKafkaConsumer.OCR_REQUEST_TOPICS;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
import uk.gov.companieshouse.ocrapiconsumer.request.OcrApiConsumerService;

@Unit
@ExtendWith(MockitoExtension.class)
public class OcrApiConsumerKafkaConsumerTest {

    private static final String CONTEXT_ID = "1";

    @Mock
    private SerializerFactory serializerFactory;
    @Mock
    private AvroSerializer serializer;
    @Mock
    private OcrApiConsumerKafkaProducer kafkaProducer;
    @Mock
	private OcrApiConsumerService ocrApiConsumerService;

    @InjectMocks
    private OcrApiConsumerKafkaConsumer kafkaConsumer;

    @BeforeEach
    public void setup() {
        this.kafkaConsumer = new OcrApiConsumerKafkaConsumer(serializerFactory, kafkaProducer, ocrApiConsumerService);
    }

    @Test
    @DisplayName("Successfully handle a message published on the Main ocr-request")
    public void shouldProcessOcrApiRequest() {

        // Given
        org.springframework.messaging.Message<OcrRequestMessage> message = createTestMessage(OCR_REQUEST_TOPICS);

        // When
        kafkaConsumer.consumeOcrApiRequestMessage(message, new ConsumerRecordMetadata(getRecordMetadata(), null));

        // Then
        verify(ocrApiConsumerService).ocrRequest(message.getPayload());
    }


    // Test that we re-try a message when we get a RetryableErrorException
    @Test
    @DisplayName("Add message to retry topic when we get a retryable error on the main topic")
    public void sentMessageToRetryTopicAfterRetryableErrorOnMainTopic() throws SerializationException,
            ExecutionException, InterruptedException {

        // Given
        org.springframework.messaging.Message<OcrRequestMessage> message = createTestMessage(OCR_REQUEST_TOPICS);
        doThrow(new uk.gov.companieshouse.ocrapiconsumer.kafka.exception.RetryableErrorException("Dummy", new Exception("dummy cause")))
				.when(ocrApiConsumerService).ocrRequest(message.getPayload());
        when(serializerFactory.getGenericRecordSerializer(OcrRequestMessage.class)).thenReturn(serializer);
        when(serializer.toBinary(any())).thenReturn(new byte[4]);

        // When
        kafkaConsumer.consumeOcrApiRequestMessage(message, new ConsumerRecordMetadata(getRecordMetadata(), null));

        // Then
        verify(kafkaProducer).sendMessage(any());
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

    private RecordMetadata getRecordMetadata() {

        TopicPartition topicPartition = new TopicPartition("test",1);  
        return new RecordMetadata(topicPartition, 0,0,0,Long.valueOf(0),0, 0);

    }
}
