package uk.gov.companieshouse.ocrapiconsumer.kafka;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.kafka.exceptions.ProducerConfigException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;
import uk.gov.companieshouse.ocrapiconsumer.groups.Unit;


@Unit
@ExtendWith(MockitoExtension.class)
public class OcrApiConsumerKafkaProducerTest {

    @InjectMocks
    private TestOcrProducer ocrApiConsumerKafkaProducer = new TestOcrProducer();

    @Mock
    private CHKafkaProducer chKafkaProducer;

    private class TestOcrProducer extends OcrApiConsumerKafkaProducer {
        ProducerConfig produceConfig = new ProducerConfig();

        protected ProducerConfig createProducerConfig() {
            return produceConfig;
        }

        protected CHKafkaProducer createChKafkaProducer(final ProducerConfig config) {
            return new CHKafkaProducer(config);
        }
    }

    @Test
    @DisplayName("afterPropertiesSet() sets producer config properties")
    public void setUpProducerConfig() {

        ocrApiConsumerKafkaProducer.setBrokerAddresses("localhost:9092"); // hostname must be in /etc/hosts

        // When
        ocrApiConsumerKafkaProducer.afterPropertiesSet();
        ProducerConfig producerConfig = ocrApiConsumerKafkaProducer.createProducerConfig();

        // Then
        assertEquals(Acks.WAIT_FOR_ALL, producerConfig.getAcks());
        assertEquals(10, producerConfig.getRetries());
       // assertTrue(producerConfig.isRoundRobinPartitioner());

        assertNotNull(ocrApiConsumerKafkaProducer.getChKafkaProducer());
    }

    @Test
    @DisplayName("Correct Exception when no Kafka Address is present")
    public void errorWhenNoBrokerAddressConfigured() {

        // When
        ProducerConfigException exception = Assertions.assertThrows(ProducerConfigException.class,
                () -> ocrApiConsumerKafkaProducer.afterPropertiesSet());

        // Then
        final String actualMessage = exception.getMessage();
        assertThat(actualMessage, is(KafkaProducer.EXPECTED_CONFIG_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("Sens a Kakfa message using CH Kafka object")
    public void testSendMessage() throws ExecutionException, InterruptedException {

        // Given
        Message testMessage = new Message();

        // When
        ocrApiConsumerKafkaProducer.sendMessage(testMessage);

        // Then
        verify(chKafkaProducer).send(testMessage);
    }


}
