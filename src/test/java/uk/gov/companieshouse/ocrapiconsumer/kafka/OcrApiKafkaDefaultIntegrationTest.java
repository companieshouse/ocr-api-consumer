package uk.gov.companieshouse.ocrapiconsumer.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.context.TestPropertySource;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;
import uk.gov.companieshouse.ocr.OcrRequestMessage;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

@SpringBootTest
@TestPropertySource(properties={"uk.gov.companieshouse.ocrapiconsumer.error-consumer=false"})
@EmbeddedKafka
class OcrApiKafkaDefaultIntegrationTest {

    private static final String CONTEXT_ID = "1";

    @Value("${kafka.consumer.main.topic}")
    private String ocrRequestTopics;

    @Value("${kafka.consumer.retry.topic}")
    private String ocrRequestRetryTopics;

    @Value("${kafka.ocr.request.group.name}")
    private String kafkaGroupName;

    @Value("${kafka.bootstrap-servers}")
    private String brokerAddresses;

    @Autowired
    private OcrApiConsumerKafkaConsumer kafkaConsumer;

    @Autowired
    private OcrApiConsumerKafkaProducer kafkaProducer;

    private KafkaMessageListenerContainer<String, OcrRequestMessage> container;

    private BlockingQueue<ConsumerRecord<String, String>> records;

    @Autowired
    private SerializerFactory serializerFactory;

    @BeforeEach
    private void setUpTestOcrApiKafkaConsumer() {
        final Map<String, Object> consumerProperties = new HashMap<>();
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, OcrKafkaRequestDeserializer.class);
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaGroupName);
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerAddresses);

        final DefaultKafkaConsumerFactory<String, OcrRequestMessage> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProperties);

        final ContainerProperties containerProperties = new ContainerProperties(ocrRequestTopics, ocrRequestRetryTopics);

        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

        records = new LinkedBlockingQueue<>();

        container.setupMessageListener((MessageListener<String, String>) record -> {
            records.add(record);
        });

        container.start();

        ContainerTestUtils.waitForAssignment(container, 0);
    }

    @AfterEach
    public void tearDown() {
        container.stop();
    }

    // TODO: Implement test criteria, pass/fail/verify
    @Test
    void testSendOcrRequestToConsumer() throws ExecutionException, InterruptedException, SerializationException {
        org.springframework.messaging.Message<OcrRequestMessage> ocrRequestMessage =
                createOcrRequestMessage(kafkaConsumer.getMainTopicName(), 0);


        kafkaProducer.sendMessage(createTestMessage(ocrRequestMessage.getPayload(), ocrRequestTopics));

    }

    // creates a test ocr request message containing test data
    private org.springframework.messaging.Message<OcrRequestMessage> createOcrRequestMessage(String receivedTopic, int attempt) {
        return new org.springframework.messaging.Message<OcrRequestMessage>() {

            @Override
            public OcrRequestMessage getPayload() {
                return createOcrRequest();
            }

            private OcrRequestMessage createOcrRequest() {

                OcrRequestMessage ocrRequestMessage = new OcrRequestMessage();
                ocrRequestMessage.setContextId(CONTEXT_ID);
                ocrRequestMessage.setAttempt(attempt);
                // these values might need changing to reflect more 'real-world' scenarios
                ocrRequestMessage.setAppId("kafka-IT");
                ocrRequestMessage.setResponseId("kafka-IT-response-id");
                ocrRequestMessage.setImageEndpoint("http://kafka-IT-test.com");
                ocrRequestMessage.setConvertedTextEndpoint("http://kafka-IT-test.com");
                ocrRequestMessage.setCreatedAt(LocalTime.now().toString());

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

    // Creates a test kafka message for the kafka producer to send
    private uk.gov.companieshouse.kafka.message.Message createTestMessage(OcrRequestMessage ocrRequest, String topic) throws SerializationException {
        final uk.gov.companieshouse.kafka.message.Message message = new uk.gov.companieshouse.kafka.message.Message();
        final AvroSerializer<OcrRequestMessage> serializer =
                serializerFactory.getGenericRecordSerializer(OcrRequestMessage.class);
        message.setKey(ocrRequestRetryTopics);
        message.setValue(serializer.toBinary(ocrRequest));
        message.setTopic(topic);
        message.setTimestamp(new Date().getTime());

        return message;
    }
}


