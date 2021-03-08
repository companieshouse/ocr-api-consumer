package uk.gov.companieshouse.ocrapiconsumer.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication;

import java.time.Duration;

import javax.annotation.PostConstruct;

@Configuration
public class SpringConfiguration {

	private static final Logger LOG = LoggerFactory.getLogger(OcrApiConsumerApplication.APPLICATION_NAME_SPACE);

	@Value("${ocr.api.url}")
	private String ocrApiUrl;

	@Value("${kafka.consumer.main.topic}")
	private String ocrRequestTopics;

	@Value("${kafka.consumer.retry.topic}")
	private String ocrRequestRetryTopics;

	@Value("${kafka.maximum.retry.attempts}")
	private int maxRetryAttempts;

	@Value("${kafka.ocr.request.group.name}")
	private String kafkaGroupName;

	@Value("${kafka.ocr.request.retry.group.name}")
	private String kafkaConsumerRetryGroupName;

	@Value("${kafka.consumer.main.topic.concurrency}")
	private int kafkaConsumerTopicConcurrency;

	@Value("${kafka.consumer.retry.topic.concurrency}")
	private int kafkaConsumerRetryTopicConcurrency;

	@Value("${kafka.retry.throttle.rate.seconds}")
	private long kafkaRetryThrottleRateSeconds;

	@Value("${kafka.bootstrap-servers}")
	private String kafkaBroker;

    @Value("${kafka.consumer.max.poll.interval.ms}")
    private int maxPollIntervalMs;

    @Value("${ocr.request.timeout.seconds}")
    protected int ocrRequestTimeoutSeconds;

    // Assessors for variables used in non SPEL context
    private int getOcrRequestTimeoutSeconds() {
        return ocrRequestTimeoutSeconds;
    }

	public long getRetryThrottleRateSeconds() {
		return kafkaRetryThrottleRateSeconds;
	}

    public String getOcrRequestTopic() {
		return ocrRequestTopics;
	}

	public String getOcrRequestTopicRetry() {
		return ocrRequestRetryTopics;
	}

	public int getMaximumRetryAttempts() {
		return maxRetryAttempts;
	}

    public String getKafkaGroupName() {
        return kafkaGroupName;
    }

    public String getKafkaConsumerRetryGroupName() {
        return kafkaConsumerRetryGroupName;
    }

    public String getKafkaBroker() {
        return kafkaBroker;
    }

    public int getMaxPollIntervalMs() {
        return maxPollIntervalMs;
    }


    @Bean
    RestTemplate restTemplate(final RestTemplateBuilder restTemplateBuilder) {
        Duration timeoutInSeconds = Duration.ofSeconds(getOcrRequestTimeoutSeconds());
        return restTemplateBuilder
                .setConnectTimeout(timeoutInSeconds)
                .setReadTimeout(timeoutInSeconds)
                .build();
    }

    @Bean
    SerializerFactory serializerFactory() {
        return new SerializerFactory();
    }

    @PostConstruct
	private void displayEnvtVariables() {
		LOG.info("-------------------- Displaying Environment variables defined in spring application.properties  ----------------------------------");

		LOG.info("The value of ${ocr.api.url} is :                              " + ocrApiUrl);
		LOG.info("The value of ${ocr.request.timeout.seconds} is :              " + ocrRequestTimeoutSeconds);
		LOG.info("The value of ${kafka.consumer.main.topic} is :                " + ocrRequestTopics);
		LOG.info("The value of ${kafka.consumer.retry.topic} is :               " + ocrRequestRetryTopics);
		LOG.info("The value of ${kafka.maximum.retry.attempts} is :             " + maxRetryAttempts);
		LOG.info("The value of ${kafka.ocr.request.group.name} is :             " + kafkaGroupName);
		LOG.info("The value of ${kafka.ocr.request.retry.group.name} is :       " + kafkaConsumerRetryGroupName);
		LOG.info("The value of ${kafka.consumer.main.topic.concurrency} is :    " + kafkaConsumerTopicConcurrency);
		LOG.info("The value of ${kafka.consumer.retry.topic.concurrency} is :   " + kafkaConsumerRetryTopicConcurrency);
		LOG.info("The value of ${kafka.retry.throttle.rate.second} is :         " + kafkaRetryThrottleRateSeconds);
		LOG.info("The value of ${kafka.bootstrap-servers} is :                  " + kafkaBroker);
		LOG.info("The value of ${kafka.consumer.max.poll.interval.ms} is :      " + maxPollIntervalMs);

		LOG.info("-------------------- End displaying Environment variables defined in spring application.properties  ----------------------------------");
	}


}
