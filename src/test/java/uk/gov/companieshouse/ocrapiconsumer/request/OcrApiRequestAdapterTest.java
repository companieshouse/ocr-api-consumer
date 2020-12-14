package uk.gov.companieshouse.ocrapiconsumer.request;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.ocrapiconsumer.groups.Unit;

@Unit
@ExtendWith(MockitoExtension.class)
class OcrApiRequestAdapterTest extends TestParent {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OcrApiRequestAdapter ocrApiRequestAdapter;

    @BeforeEach
    void setupTests() {
        ExtractTextResultDTO extractTextResultDTO = createMockTextResult();
        response = new ResponseEntity<>(extractTextResultDTO, HttpStatus.CREATED);
    }

    @Test
    void testSendOcrRequestSuccessful() {
        // given
        ResponseEntity<ExtractTextResultDTO> expected = response;
        when(restTemplate.postForEntity(anyString(), any(), eq(ExtractTextResultDTO.class))).thenReturn(response);

        // when
        ResponseEntity<ExtractTextResultDTO> actual = ocrApiRequestAdapter
                .sendOcrRequestToOcrApi(MOCK_TIFF_CONTENT, RESPONSE_ID);

        // then
        assertThat(actual, is(expected));
    }
}