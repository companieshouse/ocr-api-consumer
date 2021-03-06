package uk.gov.companieshouse.ocrapiconsumer.configuration;

import static uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication.APPLICATION_NAME_SPACE;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.LogIfLevelEnabled;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocr.OcrRequestMessage;
import uk.gov.companieshouse.ocrapiconsumer.kafka.OcrKafkaRequestDeserializer;

import java.util.HashMap;
import java.util.Map;


@EnableKafka
@Configuration
public class KafkaConfig {

    private static final Logger LOG = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private String bootstrapServers;

    private int maxPollIntervalMs;

    private int maxPollRecords;

    @Autowired
    private final SpringConfiguration springConfiguration;

    @Autowired
    public KafkaConfig(SpringConfiguration springConfiguration) {

        this.springConfiguration = springConfiguration;

        bootstrapServers = this.springConfiguration.getKafkaBroker();
        maxPollIntervalMs = this.springConfiguration.getMaxPollIntervalMs();       
        maxPollRecords = this.springConfiguration.getMaxPollRecords();
    }

    @Bean
    public ConsumerFactory<String, OcrRequestMessage> consumerFactory() {
        LOG.info("Using Bootstrap servers [" + bootstrapServers + "]");

        return new DefaultKafkaConsumerFactory<>(consumerConfigs(), 
            new StringDeserializer(),
            new OcrKafkaRequestDeserializer<>());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OcrRequestMessage> kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, OcrRequestMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setCommitLogLevel(LogIfLevelEnabled.Level.INFO);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        return factory;
    }


    private Map<String, Object> consumerConfigs() {

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, OcrKafkaRequestDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalMs);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);

        return props;
    }
}
