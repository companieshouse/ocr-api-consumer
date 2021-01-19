package uk.gov.companieshouse.ocrapiconsumer.kafka;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;
import uk.gov.companieshouse.ocrapiconsumer.logging.LoggingUtils;

/**
 *  OcrApiConsumerProducer is a wra
 */
@Service
public class OcrApiConsumerKafkaProducer extends KafkaProducer {
    /**
     * Sends message to Kafka topic
     * @param message message
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void sendMessage(final Message message) throws ExecutionException, InterruptedException {
        Map<String, Object> logMap = LoggingUtils.createLogMapWithKafkaMessage(message);
        LoggingUtils.getLogger().info("Sending message to kafka topic", logMap);
        getChKafkaProducer().send(message);
    }

    @Override
    protected void modifyProducerConfig(final ProducerConfig producerConfig) {
        producerConfig.setRequestTimeoutMilliseconds(3000);
    }
}
