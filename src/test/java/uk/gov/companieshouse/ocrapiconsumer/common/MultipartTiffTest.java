package uk.gov.companieshouse.ocrapiconsumer.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.ocrapiconsumer.groups.Unit;

import java.io.IOException;
import java.io.InputStream;

@Unit
class MultipartTiffTest {

    byte[] tiffContent = new byte[] {0, 1, 2};

    @Test
    void testCreateMultipartTiffWithoutFilename() {
        // given
        String expectedFileName = "";

        // when
        MultipartTiff actual = new MultipartTiff(tiffContent);

        // then
        assertThat(actual.getName(), is(expectedFileName));
        assertThat(actual.getBytes(), is(tiffContent));
    }

    @Test
    void testCreateMultipartTiffWithFilename() {
        // given
        String expectedFileName = "test";

        // when
        MultipartTiff actual = new MultipartTiff(expectedFileName, tiffContent);

        // then
        assertThat(actual.getName(), is(expectedFileName));
        assertThat(actual.getBytes(), is(tiffContent));
    }

    @Test
    void testGetOriginalFilenameIsEmpty() {
        // given
        String expected = "";

        // when
        MultipartTiff multipartTiff = new MultipartTiff(tiffContent);
        String actual = multipartTiff.getOriginalFilename();

        // then
        assertThat(actual, is(expected));
    }

    @Test
    void testGetSizeOfMultipartTiff() {
        // given
        long expected = 3L;
        MultipartTiff multipartTiff = new MultipartTiff(tiffContent);

        // when
        long actual = multipartTiff.getSize();

        // then
        assertThat(actual, is(expected));
    }

    @Test
    void testGetContentType() {
        // given
        String expected = "image/tiff";
        MultipartTiff multipartTiff = new MultipartTiff(tiffContent);

        // when
        String actual = multipartTiff.getContentType();

        // then
        assertThat(actual, is(expected));
    }

    @Test
    void testMultipartTiffGetSizeWhenEmpty() {
        // given
        tiffContent = new byte[0];
        long expected = 0L;

        // when
        MultipartTiff multipartTiff = new MultipartTiff(tiffContent);
        long actual = multipartTiff.getSize();

        // then
        assertThat(actual, is(expected));
    }

    @Test
    void testMultipartTiffGetSizeWhenNotEmpty() {
        // given
        long expected = 3L;

        // when
        MultipartTiff multipartTiff = new MultipartTiff(tiffContent);
        long actual = multipartTiff.getSize();

        // then
        assertThat(actual, is(expected));
    }

    @Test
    void testMultipartTiffGetSizeWhenByteArrayNullThrowsNullPointerException() {
        // given
        tiffContent = null;

        // when
        MultipartTiff multipartTiff = new MultipartTiff(tiffContent);

        // then
        assertThrows(NullPointerException.class, multipartTiff::getSize);
    }

    @Test
    void testMultipartTiffIsEmptyWhenContentIsEmpty() {
        // given
        tiffContent = new byte[]{};
        boolean expected = true;

        // when
        MultipartTiff multipartTiff = new MultipartTiff(tiffContent);
        boolean actual = multipartTiff.isEmpty();

        // then
        assertThat(actual, is(expected));
    }

    @Test
    void testMultipartTiffIsEmptyWhenContentIsNotEmpty() {
        // given
        boolean expected = false;

        // when
        MultipartTiff multipartTiff = new MultipartTiff(tiffContent);
        boolean actual = multipartTiff.isEmpty();

        // then
        assertThat(actual, is(expected));
    }

    @Test
    void testMultipartTiffIsEmptyWhenContentIsNull() {
        // given
        tiffContent = null;
        boolean expected = true;

        // when
        MultipartTiff multipartTiff = new MultipartTiff(tiffContent);
        boolean actual = multipartTiff.isEmpty();

        // then
        assertThat(actual, is(expected));
    }

    @Test
    void testMultipartTiffGetBytes() {
        // given
        MultipartTiff multipartTiff = new MultipartTiff(tiffContent);
        byte[] expected = tiffContent;

        // when
        byte[] actual = multipartTiff.getBytes();

        // then
        assertThat(actual, is(expected));
    }

    @Test
    void testMultipartTiffGetInputStream() throws IOException {
        // given
        MultipartTiff multipartTiff = new MultipartTiff(tiffContent);

        // when
        InputStream byteArrayInputStream = multipartTiff.getInputStream();
        byteArrayInputStream.close();

        // then
        assertNotNull(byteArrayInputStream);
    }
}
