package uk.gov.companieshouse.ocrapiconsumer.request;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication;

/**
 *  This class handles exceptions from void async methods (logging them via the CH Structured logging library)
 */
public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OcrApiConsumerApplication.APPLICATION_NAME_SPACE);

    @Override
    public void handleUncaughtException(
      Throwable throwable, Method method, Object... objects) {
 
        Map<String,Object> details = new HashMap<>();
        for (Object value : objects) {
            details.put("parameter value", value);
        }

        if (throwable instanceof Exception) {
            LOG.error("Asynchronous error", (Exception) throwable, details);
        }
        else {
            // Error object
            LOG.error("Error Object caught " + throwable.getMessage(), details);
        }
    }
    
}
