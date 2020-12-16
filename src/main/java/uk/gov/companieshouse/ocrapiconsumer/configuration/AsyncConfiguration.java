package uk.gov.companieshouse.ocrapiconsumer.configuration;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication;
import uk.gov.companieshouse.ocrapiconsumer.request.CustomAsyncExceptionHandler;

/**
 *  This configuration adds asynchronous support for this application and also 
 *  supports logging when async methods have void calls
 */
@EnableAsync
@Configuration
public class AsyncConfiguration  extends AsyncConfigurerSupport {

    private static final Logger LOG = LoggerFactory.getLogger(OcrApiConsumerApplication.APPLICATION_NAME_SPACE);

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {

        LOG.debug("Sync error found - passing to the handler");

        return new CustomAsyncExceptionHandler();
        
    }
}
