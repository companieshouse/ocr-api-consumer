package gov.uk.companieshouse.ocrapiconsumer.configuration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@SpringBootConfiguration
@EnableAsync
public class SpringConfiguration {

    @Bean
    ResponseErrorHandler responseErrorHandler() {
        return new DefaultResponseErrorHandler();
    }

    @Bean
    RestTemplate restTemplate(final RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }
}
