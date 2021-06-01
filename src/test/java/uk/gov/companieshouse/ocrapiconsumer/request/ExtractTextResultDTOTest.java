package uk.gov.companieshouse.ocrapiconsumer.request;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import org.junit.jupiter.api.Test;

class ExtractTextResultDTOTest extends TestParent {

    @Test
    void testCreateErrorExtractTextResultDTOFromContextId() {
        ExtractTextResultDTO extractTextResultDTO = ExtractTextResultDTO.createErrorExtractTextResultDTOFromContextId(CONTEXT_ID, RESPONSE_ID);

        assertThat(extractTextResultDTO.getAverageConfidenceScore(), is(0));
        assertThat(extractTextResultDTO.getLowestConfidenceScore(), is(0));
        assertThat(extractTextResultDTO.getOcrProcessingTimeMs(), is(0L));
        assertThat(extractTextResultDTO.getTotalProcessingTimeMs(), is(0L));
        assertThat(extractTextResultDTO.getContextId(), is(CONTEXT_ID));
        assertThat(extractTextResultDTO.getResponseId(), is(RESPONSE_ID));
        assertThat(extractTextResultDTO.getExtractedText(), is(ExtractTextResultDTO.OCR_CONVERSION_ERROR_TEXT));
        assertThat(extractTextResultDTO.getResultCode(), is(ExtractTextResultDTO.OCR_CONVERSION_ERROR_CODE));
    }
}
