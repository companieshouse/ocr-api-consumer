package uk.gov.companieshouse.ocrapiconsumer.request;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class OcrApiConsumerServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OcrApiConsumerService ocrApiConsumerService;

    //TODO: Add tests (removed due to changing most of the service)

}
