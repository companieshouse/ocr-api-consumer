package uk.gov.companieshouse.ocrapiconsumer.request.image;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.ocrapiconsumer.groups.Unit;
import uk.gov.companieshouse.ocrapiconsumer.request.TestParent;

import java.io.IOException;

@Unit
@ExtendWith(MockitoExtension.class)
class ChipsImageAdapterTest extends TestParent {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ChipsImageAdapter chipsImageAdapter;

    @Test
    void testGetTiffImageSuccessfully() throws IOException {
        // given
        byte[] expected = MOCK_TIFF_CONTENT;
        when(restTemplate.getForEntity(IMAGE_ENDPOINT, byte[].class))
                .thenReturn(new ResponseEntity<>(MOCK_TIFF_CONTENT, HttpStatus.OK));

        // when
        byte[] actual = chipsImageAdapter.getTiffImageFromChips(IMAGE_ENDPOINT);

        // then
        assertThat(actual, is(expected));
    }

    @Test
    void testGetTiffImageThrowsIOException() {
        // given
        when(restTemplate.getForEntity(IMAGE_ENDPOINT, byte[].class))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        // then
        assertThrows(IOException.class, () -> chipsImageAdapter.getTiffImageFromChips(IMAGE_ENDPOINT));

    }
}
