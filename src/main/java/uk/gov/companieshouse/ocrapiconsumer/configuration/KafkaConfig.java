package uk.gov.companieshouse.ocrapiconsumer.configuration;

import static uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication.APPLICATION_NAME_SPACE;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocr.OcrRequestMessage;
import uk.gov.companieshouse.ocrapiconsumer.common.EnvironmentVariable;
import uk.gov.companieshouse.ocrapiconsumer.kafka.OcrKafkaRequestDeserializer;

import java.util.HashMap;
import java.util.Map;


@EnableKafka
@Configuration
public class KafkaConfig {

    private static final Logger LOG = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, OcrRequestMessage> consumerFactory(final EnvironmentReader environmentReader) {
        bootstrapServers = getBootstrapServers(environmentReader);
        LOG.info("Using Bootstrap servers [" + bootstrapServers + "]");

        return new DefaultKafkaConsumerFactory<>(consumerConfigs(), 
            new StringDeserializer(),
            new OcrKafkaRequestDeserializer<>());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OcrRequestMessage> kafkaListenerContainerFactory
            (final EnvironmentReader environmentReader) {

        ConcurrentKafkaListenerContainerFactory<String, OcrRequestMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory(environmentReader));

        return factory;
    }


    private Map<String, Object> consumerConfigs() {

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, OcrKafkaRequestDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return props;
    }

    private String getBootstrapServers(final EnvironmentReader environmentReader) {
        return environmentReader.getMandatoryString(EnvironmentVariable.KAFKA_BROKER_ADDR.name());
    }
}
