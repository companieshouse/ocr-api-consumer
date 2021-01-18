package uk.gov.companieshouse.ocrapiconsumer.kafka;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.GenericErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    private static final Logger LOG = LoggerFactory.getLogger(OcrApiConsumerApplication.APPLICATION_NAME_SPACE);

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Bean
    public ConsumerFactory<String, OcrKafkaRequest> consumerFactory() {

        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // props.put(ConsumerConfig.GROUP_ID_CONFIG, "group-id");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, OcrKafkaRequestDeserializer.class);
        // props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new OcrKafkaRequestDeserializer<>());
        // return new DefaultKafkaConsumerFactory<String, OcrKafkaRequest>(props, new StringDeserializer(), new JsonDeserializer<OcrKafkaRequest>());
        // return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new OcrKafkaRequestDeserializer<>());

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new OcrKafkaRequestDeserializer<>());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OcrKafkaRequest> kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, OcrKafkaRequest> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());

        factory.setErrorHandler(((exception, data) -> {
            LOG.error("data is null: " + Boolean.toString(data == null));
            // LOG.error("Contents: " + data.toString());
            LOG.error("Exception logging");
            LOG.error(exception);
        }));

        return factory;
    }
}
