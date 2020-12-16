# ocr-api-consumer

Service to consume requests for extraction of text from images and manage the requests to the OCR API. Drop 1 will NOT include any Kafka.

## Requirements

- Java 8 (drop 2 will use 11)
- Maven

## Usage

- Run `make dev` to build JAR (versioned in target and unversioned in top level d) and run the unit tests
- Run `java -jar ocr-api-consumer.jar` to run the application
- Alternatively, `export OCR_API_CONSUMER_PORT={your-chosen-port-number}` and run ./start.sh to run the application on your chosen port number.

## Environment Variables

The following is a list of mandatory environment variables for the service to run:

Name                                        | Description                         | Example Value
------------------------------------------- | ----------------------------------- | -------------------------------------------------------------------------
OCR_API_URL                                 | The URL of the ocr-api              | http://localhost:8080/api/ocr/image/tiff/extractText  (default value)

## Testing Locally (dev)

### Testing with postman

Send a post request to http://localhost:9090/internal/ocr-requests with the following JSON body (each field is mandatory):
```
{
  "image_endpoint": "http://testurl.com/cff/servlet/viewArticles?transaction_id=9613245852",
  "converted_text_endpoint": "http://testurl.com/ocr-results/",
  "response_id": "9613245852"
}
```

### Testing with curl
```
curl --header "Content-Type: application/json" \
--request POST \
--data '{"image_endpoint": "http://testurl.com/cff/servlet/viewArticles?transaction_id=9613245852", "converted_text_endpoint": "http://testurl.com/ocr-results/", "response_id": "9613245852"}' \
http://localhost:9090/internal/ocr-requests
```

### Running tests with Maven

You are able to test with Maven by running:

``` bash
mvn test
```

To test just integration tests or just unit tests run:

``` bash
mvn test -Dincluded.tests=integration-test
```

``` bash
mvn test -Dincluded.tests=unit-test
```
