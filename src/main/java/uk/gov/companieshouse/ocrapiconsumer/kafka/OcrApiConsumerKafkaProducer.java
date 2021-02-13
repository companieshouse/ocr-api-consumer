package uk.gov.companieshouse.ocrapiconsumer.kafka;

import static uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication.APPLICATION_NAME_SPACE;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.concurrent.ExecutionException;

/**
 *  OcrApiConsumerProducer ultimately wraps the CH Kafka Producer 
 */
@Service
public class OcrApiConsumerKafkaProducer extends KafkaProducer {

    private static final Logger LOG = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private static final int REQUEST_TIMEOUT_MILLISECONDS = 3000;

    /**
     * Sends message to Kafka topic
     * @param message message
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void sendMessage(final Message message) throws ExecutionException, InterruptedException {
        LOG.info("Sending message to kafka topic");
        getChKafkaProducer().send(message);
    }

    @Override
    protected void modifyProducerConfig(final ProducerConfig producerConfig) {
        producerConfig.setRequestTimeoutMilliseconds(REQUEST_TIMEOUT_MILLISECONDS);
    }
}
