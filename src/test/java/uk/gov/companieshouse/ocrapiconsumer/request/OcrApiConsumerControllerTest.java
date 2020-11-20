package uk.gov.companieshouse.ocrapiconsumer.request;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.ocrapiconsumer.groups.Unit;

@Unit
@ExtendWith(MockitoExtension.class)
class OcrApiConsumerControllerTest {

    private static final String EXTERNAL_REFERENCE_ID = "abc";
    private static final String IMAGE_ENDPOINT = "https://image-endpoint";
    private static final String CONVERTED_TEXT_ENDPOINT = "https://converted-text-endpoint";

    @Mock
    private OcrApiConsumerService service;

    @InjectMocks
    private OcrApiConsumerController controller;

    @Test
    void testReceiveOcrRequestReturns202() {
        // given
        var expected = new ResponseEntity<HttpStatus>(HttpStatus.ACCEPTED);

        // when
        var actual = controller
                .receiveOcrRequest(EXTERNAL_REFERENCE_ID,IMAGE_ENDPOINT,CONVERTED_TEXT_ENDPOINT);

        // then
        verify(service).logOcrRequest(EXTERNAL_REFERENCE_ID, IMAGE_ENDPOINT, CONVERTED_TEXT_ENDPOINT);
        assertThat(actual, is(expected));
    }
}