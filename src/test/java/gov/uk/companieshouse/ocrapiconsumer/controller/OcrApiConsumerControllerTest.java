package gov.uk.companieshouse.ocrapiconsumer.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import gov.uk.companieshouse.ocrapiconsumer.service.OcrApiConsumerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class OcrApiConsumerControllerTest {

    @Mock
    private OcrApiConsumerService service;

    @Mock
    private Logger logger;

    @InjectMocks
    private OcrApiConsumerController controller;


    @Test
    void testRequestLogged() {
        // when
        ResponseEntity<String> response = new ResponseEntity<>(HttpStatus.CREATED);
        when(service.sendRequestToOcrMicroservice(anyString(), anyString(), anyString())).thenReturn(response);

        controller.receiveOcrRequest("1",
                "image-endpoint",
                "text-endoint");

        // then
        verify(service).logOcrRequest(anyString(), anyString(), anyString());
    }
}
