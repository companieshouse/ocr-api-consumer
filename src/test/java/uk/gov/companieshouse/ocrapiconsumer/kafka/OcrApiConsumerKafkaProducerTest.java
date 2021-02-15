package uk.gov.companieshouse.ocrapiconsumer.kafka;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.exception.EnvironmentVariableException;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;
import uk.gov.companieshouse.kafka.exceptions.ProducerConfigException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;
import uk.gov.companieshouse.ocrapiconsumer.common.EnvironmentVariable;
import uk.gov.companieshouse.ocrapiconsumer.groups.Unit;


@Unit
@ExtendWith(MockitoExtension.class)
class OcrApiConsumerKafkaProducerTest {

    private static final String DUMMY_KAFKA_BROKER_ADDR = "localhost:9092";

    @Mock
    private EnvironmentReader environmentReader;

    @Mock
    private CHKafkaProducer chKafkaProducer;

    @InjectMocks
    private TestOcrProducer ocrApiConsumerKafkaProducer = new TestOcrProducer();


    private class TestOcrProducer extends OcrApiConsumerKafkaProducer {
        ProducerConfig produceConfig = new ProducerConfig();

        protected ProducerConfig createProducerConfig() {
            return produceConfig;
        }

        protected CHKafkaProducer createChKafkaProducer(final ProducerConfig config) {
            return new CHKafkaProducer(config);
        }

        protected EnvironmentReader createEnvironmentReader() {
            return environmentReader;
        }
    }

    @Test
    @DisplayName("afterPropertiesSet() sets producer config properties")
    void setUpProducerConfig() {

        doReturn(DUMMY_KAFKA_BROKER_ADDR)
                .when(environmentReader).getMandatoryString(EnvironmentVariable.KAFKA_BROKER_ADDR.name());
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
    void errorWhenNoBrokerAddressConfigured() {
        // given

        // simulates a mandatory env var returning null or empty
        doThrow(EnvironmentVariableException.class)
                .when(environmentReader).getMandatoryString(EnvironmentVariable.KAFKA_BROKER_ADDR.name());

        // When
        ProducerConfigException exception = Assertions.assertThrows(ProducerConfigException.class,
                () -> ocrApiConsumerKafkaProducer.afterPropertiesSet());

        // Then
        final String actualMessage = exception.getMessage();
        assertThat(actualMessage, is(KafkaProducer.EXPECTED_CONFIG_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("Sends a Kakfa message using CH Kafka object")
    void testSendMessage() throws ExecutionException, InterruptedException {

        // Given
        Message testMessage = new Message();

        // When
        ocrApiConsumerKafkaProducer.sendMessage(testMessage);

        // Then
        verify(chKafkaProducer).send(testMessage);
    }


}
