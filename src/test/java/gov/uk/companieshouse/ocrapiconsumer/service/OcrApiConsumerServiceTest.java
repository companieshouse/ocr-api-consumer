package gov.uk.companieshouse.ocrapiconsumer.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class OcrApiConsumerServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private Logger logger;

    private OcrApiConsumerService ocrApiConsumerService;

    @BeforeEach
    void setup() {
        ocrApiConsumerService = new OcrApiConsumerServiceImpl(logger, restTemplate);
    }

    @Test
    void testLogRequestSuccess() {
        // TODO: Add test for mocked logger
    }

    @Test
    void testSendRequestToOcrService() {
        // given
        ResponseEntity<String> expected = new ResponseEntity<>(HttpStatus.CREATED);

        // when
        ResponseEntity<String> response = new ResponseEntity<>(HttpStatus.CREATED);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(response);

        // then
        ResponseEntity<String> actual = ocrApiConsumerService.sendRequestToOcrMicroservice(anyString(),
                anyString(), anyString());
        assertThat(actual, is(expected));

    }


}
