package uk.gov.companieshouse.ocrapiconsumer.healthcheck;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/healthcheck")
    public ResponseEntity<ServiceStatus> isHealthy() {
        return new ResponseEntity<>(new ServiceStatus("ALIVE"), HttpStatus.OK);
    }

    static final class ServiceStatus {
        private final String status;

        public ServiceStatus(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }
}