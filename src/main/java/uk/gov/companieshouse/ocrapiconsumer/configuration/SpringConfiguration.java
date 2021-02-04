package uk.gov.companieshouse.ocrapiconsumer.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;

@Configuration
public class SpringConfiguration {

    private static final int DEFAULT_REQUEST_TIMEOUT = 300_000;

    private SimpleClientHttpRequestFactory getSimpleClientHttpRequestFactory() {
        final int timeout = getTimeout();

        SimpleClientHttpRequestFactory clientHttpRequestFactory
                = new SimpleClientHttpRequestFactory();

        //Connect timeout
        clientHttpRequestFactory.setConnectTimeout(timeout);

        //Read timeout
        clientHttpRequestFactory.setReadTimeout(timeout);
        return clientHttpRequestFactory;
    }

    private int getTimeout() {
        final EnvironmentReader environmentReader = new EnvironmentReaderImpl();
        Integer timeout = environmentReader.getOptionalInteger("OCR_REQUEST_TIMEOUT");
        return timeout == null ? DEFAULT_REQUEST_TIMEOUT : timeout;
    }

    @Bean
    RestTemplate restTemplate(final RestTemplateBuilder restTemplateBuilder) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.setRequestFactory(getSimpleClientHttpRequestFactory());
        return restTemplate;
    }

    @Bean
    SerializerFactory serializerFactory() {
        return new SerializerFactory();
    }
}
