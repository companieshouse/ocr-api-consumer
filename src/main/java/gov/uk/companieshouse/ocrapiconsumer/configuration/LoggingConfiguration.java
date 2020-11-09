package gov.uk.companieshouse.ocrapiconsumer.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Configuration
@PropertySource("classpath:logging.properties")
public class LoggingConfiguration {

    @Value("${logger.namespace}")
    private String loggerNamespace;


    public LoggingConfiguration() {
        // empty no-args constructor required
    }

    /**
     * Creates a logger with specified namespace.
     *
     * @return the {@link LoggerFactory} for the specified namespace
     */
    @Bean
    public Logger logger() {
        return LoggerFactory.getLogger(loggerNamespace);
    }

}
