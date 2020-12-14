package uk.gov.companieshouse.ocrapiconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OcrApiConsumerApplication {


	public static final String APPLICATION_NAME_SPACE = "ocr-api-consumer";

	public static void main(String[] args) {
		SpringApplication.run(OcrApiConsumerApplication.class, args);
	}
}
