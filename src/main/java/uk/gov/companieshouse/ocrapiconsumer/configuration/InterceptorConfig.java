package uk.gov.companieshouse.ocrapiconsumer.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;


@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger("ocr-api-consumer");

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {

        registry.addInterceptor(new LoggingInterceptor(LOGGER));
        
    }
}