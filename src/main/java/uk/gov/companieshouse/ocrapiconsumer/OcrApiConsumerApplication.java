package uk.gov.companieshouse.ocrapiconsumer;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@SpringBootApplication
public class OcrApiConsumerApplication {

	public static final String APPLICATION_NAME_SPACE = "ocr-api-consumer";
	private static final Logger LOG = LoggerFactory.getLogger(OcrApiConsumerApplication.APPLICATION_NAME_SPACE);

	@Value("${ocr.api.url}")
	private String ocrApiUrl;

	@Value("${ocr.request.timeout.seconds}")
	private String ocrRequestTimeout;

	@Value("${kafka.consumer.main.topic}")
	private String kafkaConsumerMainTopic;

	@Value("${kafka.consumer.retry.topic}")
	private String kafkaConsumerRetryTopic;

	@Value("${kafka.maximum.retry.attempts}")
	private String kafkaMaxRetryAttempts;

	@Value("${kafka.ocr.request.group.name}")
	private String kafkaGroupName;

	@Value("${kafka.ocr.request.retry.group.name}")
	private String kafkaConsumerRetryGroupName;

	@Value("${kafka.consumer.main.topic.concurrency}")
	private String kafkaConsumerTopicConcurrency;

	@Value("${kafka.consumer.retry.topic.concurrency}")
	private String kafkaConsumerRetryTopicConcurrency;

	@Value("${kafka.retry.throttle.rate.seconds}")
	private String kafkaRetryThrottleRate;

	@Value("${kafka.bootstrap-servers}")
	private String kafkaBroker;

	public static void main(String[] args) {
		SpringApplication.run(OcrApiConsumerApplication.class, args);
	}

	@PostConstruct
	private void displayEnvtVariables() {
		LOG.info("-------------------- Displaying Environment variables defined in spring application.properties  ----------------------------------");

		LOG.info("The value of ${ocr.api.url} is" + " : " + ocrApiUrl);
		LOG.info("The value of ${ocr.request.timeout.seconds} is" + " : " + ocrRequestTimeout);
		LOG.info("The value of ${kafka.consumer.main.topic} is" + " : " + kafkaConsumerMainTopic);
		LOG.info("The value of ${kafka.consumer.retry.topic} is" + " : " + kafkaConsumerRetryTopic);
		LOG.info("The value of ${kafka.maximum.retry.attempts} is" + " : " + kafkaMaxRetryAttempts);
		LOG.info("The value of ${kafka.ocr.request.group.name} is" + " : " + kafkaGroupName);
		LOG.info("The value of ${kafka.ocr.request.retry.group.name} is" + " : " + kafkaConsumerRetryGroupName);
		LOG.info("The value of ${kafka.consumer.main.topic.concurrency} is" + " : " + kafkaConsumerTopicConcurrency);
		LOG.info("The value of ${kafka.consumer.retry.topic.concurrency} is" + " : "
				+ kafkaConsumerRetryTopicConcurrency);
		LOG.info("The value of ${kafka.retry.throttle.rate.second} is" + " : " + kafkaRetryThrottleRate);
		LOG.info("The value of ${kafka.bootstrap-servers} is" + " : " + kafkaBroker);

		LOG.info("-------------------- End displaying Environment variables defined in spring application.properties  ----------------------------------");
	}

}
