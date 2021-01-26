package uk.gov.companieshouse.ocrapiconsumer.kafka;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import static uk.gov.companieshouse.ocrapiconsumer.kafka.OcrApiConsumerKafkaConsumer.OCR_REQUEST_TOPICS;
import static uk.gov.companieshouse.ocrapiconsumer.kafka.OcrApiConsumerKafkaConsumer.OCR_REQUEST_RETRY_TOPICS;
import static uk.gov.companieshouse.ocrapiconsumer.kafka.OcrApiConsumerKafkaConsumer.OCR_REQUEST_ERROR_TOPICS;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.messaging.MessageHeaders;

import uk.gov.companieshouse.kafka.exceptions.ProducerConfigException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;
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
    private OcrApiConsumerKafkaProducer kafkaProducer;
    @Mock
    private OcrApiConsumerService ocrApiConsumerService;
    @Mock
    private KafkaListenerEndpointRegistry registry;

    @InjectMocks
    private OcrApiConsumerKafkaConsumer kafkaConsumer;

    @BeforeEach
    public void setup() {
        this.kafkaConsumer = new OcrApiConsumerKafkaConsumer(serializerFactory, kafkaProducer, ocrApiConsumerService, registry);
    }

    @Test
    @DisplayName("Successfully handle a message published on the Main ocr-request")
    public void shouldProcessOcrApiRequest() {

        // Given
        org.springframework.messaging.Message<OcrRequestMessage> message = createTestMessage(OCR_REQUEST_TOPICS);

        // When
        kafkaConsumer.processOcrApiRequest(message);

        // Then
        verify(ocrApiConsumerService).ocrRequest(message.getPayload());
    }

    @Test
    @DisplayName("Successfully handle a message published on the Retry ocr-request")
    public void shouldRetryOcrApiRequest() {

        // Given
        org.springframework.messaging.Message<OcrRequestMessage> message = createTestMessage(OCR_REQUEST_RETRY_TOPICS);

        // When
        kafkaConsumer.processOcrApiRequest(message);

        // Then
        verify(ocrApiConsumerService).ocrRequest(message.getPayload());
    }

    @Test
    @DisplayName("Successfully error an api request")
    public void shouldErrorOcrApiRequest() {
        // Given
        org.springframework.messaging.Message<OcrRequestMessage> message = createTestMessage(OCR_REQUEST_ERROR_TOPICS);

        // When
        kafkaConsumer.processOcrApiRequest(message);

        // Then
        verify(ocrApiConsumerService).ocrRequest(message.getPayload());
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
}
