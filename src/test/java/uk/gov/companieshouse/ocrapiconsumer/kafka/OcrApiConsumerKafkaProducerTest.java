package uk.gov.companieshouse.ocrapiconsumer.kafka;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javafx.beans.binding.When;

import uk.gov.companieshouse.kafka.exceptions.ProducerConfigException;
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class OcrApiConsumerKafkaProducerTest {

	@InjectMocks
	private TestOcrProducer ocrApiConsumerKafkaProducer = new TestOcrProducer();

	@Mock
    private CHKafkaProducer chKafkaProducer;

	
	private class TestOcrProducer extends OcrApiConsumerKafkaProducer{
		ProducerConfig produceConfig = new ProducerConfig();

		protected ProducerConfig createProducerConfig() {
			return produceConfig;
		}
	}

	@Test
	@DisplayName("afterPropertiesSet() sets producer config properties")
	public void setUpProducerConfig() {

		ocrApiConsumerKafkaProducer.setBrokerAddresses("localhost:9092"); //hostname must be in /etc/hosts

		// When
		ocrApiConsumerKafkaProducer.afterPropertiesSet();
		ProducerConfig producerConfig = ocrApiConsumerKafkaProducer.createProducerConfig();

		// Then
		assertEquals(Acks.WAIT_FOR_ALL, producerConfig.getAcks());
		assertEquals(10, producerConfig.getRetries());
		assertTrue(producerConfig.isRoundRobinPartitioner());

	}
}
