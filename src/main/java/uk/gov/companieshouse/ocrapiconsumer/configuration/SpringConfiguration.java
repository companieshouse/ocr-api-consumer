package uk.gov.companieshouse.ocrapiconsumer.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;
import uk.gov.companieshouse.ocrapiconsumer.common.EnvironmentVariable;

import java.time.Duration;

@Configuration
public class SpringConfiguration {

    protected static final int DEFAULT_REQUEST_TIMEOUT_SECONDS = 300;

    private int getTimeout(final EnvironmentReader environmentReader) {
        Integer timeout = environmentReader.getOptionalInteger(EnvironmentVariable.OCR_REQUEST_TIMEOUT_SECONDS.name());
        return timeout == null ? DEFAULT_REQUEST_TIMEOUT_SECONDS : timeout;
    }

    @Bean
    RestTemplate restTemplate(final RestTemplateBuilder restTemplateBuilder,
                              final EnvironmentReader environmentReader) {
        Duration timeoutInSeconds = Duration.ofSeconds(getTimeout(environmentReader));
        return restTemplateBuilder
                .setConnectTimeout(timeoutInSeconds)
                .setReadTimeout(timeoutInSeconds)
                .build();
    }

    @Bean
    SerializerFactory serializerFactory() {
        return new SerializerFactory();
    }
}
