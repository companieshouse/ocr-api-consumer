# ocr-api-consumer

Service to consume requests for extraction of text from images and manage the requests to the OCR API.

When this application starts up it will consume messages on both the Main and Retry Topic.

There is no error topic processing so far since all clients require this data in a timely manner and so errors need to be sent back to the calling system in this case

## Requirements

- Java 8 (drop 2 will use 11)
- Maven
- Docker

## Usage without Docker

- Run `make dev` to build JAR (versioned in target and unversioned in top level d) and run the unit tests
- Run `java -jar ocr-api-consumer.jar` to run the application
- Alternatively, `export OCR_API_CONSUMER_PORT={your-chosen-port-number}` and run `./start.sh` to run the application on your chosen port number.

## Usage with Docker

The service can be run using docker, with the addition of the jib maven plugin.

- Run `mvn clean`
- Run `mvn compile jib:dockerBuild` to compile the project into a docker image
- Run `docker run -e OCR_API_URL -t -i -p 8080:8080 ocr-api-consumer:unversioned` to run the service on port 8080.

## Environment Variables

The following is a list of mandatory environment variables for the service to run:


Name                                        | Description                            | Example Value
------------------------------------------- | ------------------------------------   | -------------------------------------------------------------------------
OCR_API_URL                                 | The URL of the ocr-api                            | http://localhost:8080/api/ocr/image/tiff/extractText
KAFKA_BROKER_ADDR                           | Address of the Kafka Broker                       | localhost:9092
CONSUMER_CONCURRENCY                        | Number of consumer threads                        | 3
RETRY_THROTTLE_RATE_SECONDS                 | Number of seconds before retrying                 | 3
OCR_REQUEST_TIMEOUT_SECONDS                 | Optional request timeout for API calls            | 300 (default value)
OCR_REQUEST_TOPIC                           | The kafka request topic for the ocr-api-consumer  | ocr-request
MAXIMUM_RETRY_ATTEMPTS                      | The maximum amount of retries for the kafka topic | 3

## Testing Locally (dev)

### Testing with postman

Send a post request to `http://localhost:9090/internal/ocr-requests` with the following JSON body (each field is mandatory):

``` json
{
  "image_endpoint": "http://testurl.com/cff/servlet/viewArticles?transaction_id=9613245852",
  "converted_text_endpoint": "http://testurl.com/ocr-results/",
  "response_id": "9613245852"
}
```

### Testing with curl

``` json
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
