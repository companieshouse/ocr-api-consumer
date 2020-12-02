# ocr-api-consumer
Service to consume requests for extraction of text from images and manage the requests to the OCR API. Drop 1 will NOT include any Kafka.

## Requirements

- Java 11
- Maven

## Usage
- Run `make dev` to build JAR (versioned in target and unversioned in top level d) and run the unit tests **(using Java 11)**
- Run `java -jar ocr-api-consumer.jar` to run the application.

## Environment Variables

The following is a list of mandatory environment variables for the service to run:

Name                                        | Description                         | Example Value
------------------------------------------- | ----------------------------------- | -------------------------------------------------------------------------
OCR_API_URL                                 | The URL of the ocr-api              | http://localhost:8080/api/ocr/image/tiff/extractText  (default value)

## Testing Locally (dev)
The service runs locally on the port 9090.

### Testing with postman
Setup a post request to http://localhost:9090/ocr-requests with the following query parameters:

| Parameter Name          | Description                                                      | Example Value                                                                                        |
|-------------------------|------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| external-reference-id   | The request ID of the request used in context for logging        | ABC                                                                                                         |
| image-endpoint          | The endpoint of the image to retrieve for OCR text extraction    | http://localhost:9090/image (dummy endpoint, reads tif file from resources) |
| extracted-text-endpoint | The endpoint to send the extracted text result to once retrieved | http://localhost:9090/text (dummy endpoint, returns 200 OK)                                             |

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
