package uk.gov.companieshouse.ocrapiconsumer.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;

import java.time.Duration;

@Configuration
public class SpringConfiguration {

    private static final int DEFAULT_REQUEST_TIMEOUT = 300_000;

    private int getTimeout(final EnvironmentReader environmentReader) {
        Integer timeout = environmentReader.getOptionalInteger("OCR_REQUEST_TIMEOUT");
        return timeout == null ? DEFAULT_REQUEST_TIMEOUT : timeout;
    }

    @Bean
    EnvironmentReader environmentReader() {
        return new EnvironmentReaderImpl();
    }

    @Bean
    RestTemplate restTemplate(final RestTemplateBuilder restTemplateBuilder,
                              final EnvironmentReader environmentReader) {
        return restTemplateBuilder
                .setReadTimeout(Duration.ofMillis(getTimeout(environmentReader)))
                .build();
    }

    @Bean
    SerializerFactory serializerFactory() {
        return new SerializerFactory();
    }
}
