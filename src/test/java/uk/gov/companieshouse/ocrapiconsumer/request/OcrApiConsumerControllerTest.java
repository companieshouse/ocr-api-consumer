package uk.gov.companieshouse.ocrapiconsumer.request;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = OcrApiConsumerController.class)
class OcrApiConsumerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private OcrApiConsumerService service;

    //TODO: Add tests (removed due to changing most of the service)

}