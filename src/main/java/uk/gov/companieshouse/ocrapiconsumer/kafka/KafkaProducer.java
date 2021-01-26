package uk.gov.companieshouse.ocrapiconsumer.kafka;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.companieshouse.kafka.exceptions.ProducerConfigException;
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.ocrapiconsumer.logging.LoggingUtils;

public abstract class KafkaProducer implements InitializingBean {

    protected static final String EXPECTED_CONFIG_ERROR_MESSAGE = "Broker addresses for kafka broker missing, check if environment variable KAFKA_BROKER_ADDR is configured. "
    + "[Hint: The property 'kafka.broker.addresses' uses the value of this environment variable in live "
    + "environments and that of 'spring.embedded.kafka.brokers' property in test.]";

    protected CHKafkaProducer chKafkaProducer;

    private static final Logger LOGGER = LoggingUtils.getLogger();

    @Value("${spring.kafka.bootstrap-servers}")
    private String brokerAddresses;

    @Override
    public void afterPropertiesSet() {
        LOGGER.trace("Configuring CH Kafka producer");
        final ProducerConfig config = createProducerConfig();
        setBrokerAddress(config);
        config.setRoundRobinPartitioner(true);
        config.setAcks(Acks.WAIT_FOR_ALL);
        config.setRetries(10); 
        modifyProducerConfig(config);
        chKafkaProducer = createChKafkaProducer(config);
    }

    /**
     * Extending classes may implement this to provide any specific producer configuration modifications required.
     * @param producerConfig the producer configuration to be modified
     */
    protected void modifyProducerConfig(final ProducerConfig producerConfig) {
        // Does nothing here
    }

    protected CHKafkaProducer getChKafkaProducer() {
        return chKafkaProducer;
    }


    protected void setBrokerAddress(ProducerConfig config) {
     
        if (brokerAddresses != null && !brokerAddresses.isEmpty()) {
            config.setBrokerAddresses(brokerAddresses.split(","));
        } else {
            throw new ProducerConfigException(EXPECTED_CONFIG_ERROR_MESSAGE);
        }
    }

    /**
     * Extending classes may implement this to facilitate testing for example.
     * @param config the {@link ProducerConfig} used to configure the producer
     * @return the {@link CHKafkaProducer} created
     */
    protected CHKafkaProducer createChKafkaProducer(final ProducerConfig config) {
        return new CHKafkaProducer(config);
    }

    /**
     * Extending classes may implement this to facilitate testing for example.
     * @return the {@link ProducerConfig} created
     */
    protected ProducerConfig createProducerConfig() {
        return new ProducerConfig();
    }

    protected void setBrokerAddresses(String brokerAddresses) {
        this.brokerAddresses = brokerAddresses;
    }
}

