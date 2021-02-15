package uk.gov.companieshouse.ocrapiconsumer.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.RequestLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoggingInterceptor extends HandlerInterceptorAdapter implements RequestLogger {

    @SuppressWarnings("squid:S1312") // Loggers should be private static final; here logger is singleton bean
    private Logger logger;

    @Autowired
    public LoggingInterceptor(Logger logger) {
        this.logger = logger;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
       logStartRequestProcessing(request,logger);
       return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        logEndRequestProcessing(request, response, logger);
    }
}
