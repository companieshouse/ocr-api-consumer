package uk.gov.companieshouse.ocrapiconsumer.healthcheck;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/internal/ocr-api-consumer/healthcheck")
    public ResponseEntity<String> isHealthy() {
        return new ResponseEntity<>("ALIVE", HttpStatus.OK);
    }

}