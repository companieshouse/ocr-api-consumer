package gov.uk.companieshouse.ocrapiconsumer.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("dummy-endpoint")
public class DummyController {

    @GetMapping
    public HttpStatus dummyGetResponse() {
        return HttpStatus.OK;
    }

    @PostMapping
    public HttpStatus dummyPostResponse() {
        return HttpStatus.CREATED;
    }
}
