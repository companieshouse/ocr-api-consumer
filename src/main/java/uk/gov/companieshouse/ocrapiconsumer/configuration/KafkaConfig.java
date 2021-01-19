package uk.gov.companieshouse.ocrapiconsumer.configuration;

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

import uk.gov.companieshouse.ocr.OcrRequestMessage;
import uk.gov.companieshouse.ocrapiconsumer.kafka.OcrKafkaRequestDeserializer;

@EnableKafka
@Configuration
public class KafkaConfig {


    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Bean
    public ConsumerFactory<String, OcrRequestMessage> consumerFactory() {

        return new DefaultKafkaConsumerFactory<>(consumerConfigs(), 
            new StringDeserializer(),
            new OcrKafkaRequestDeserializer<>());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OcrRequestMessage> kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, OcrRequestMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());

        return factory;
    }


    private Map<String, Object> consumerConfigs() {

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, OcrKafkaRequestDeserializer.class);

        return props;
    }
}