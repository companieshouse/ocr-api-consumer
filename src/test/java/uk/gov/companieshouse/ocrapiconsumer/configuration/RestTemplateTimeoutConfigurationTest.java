package uk.gov.companieshouse.ocrapiconsumer.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.environment.EnvironmentReader;

import java.time.Duration;

@ExtendWith(MockitoExtension.class)
class RestTemplateTimeoutConfigurationTest {

    private static final int DEFAULT_REQUEST_TIMEOUT_SECONDS = 300;
    private static final Integer MOCK_TIMEOUT_SECONDS = 60;
    private static final String OCR_REQUEST_TIMEOUT_SECONDS = "OCR_REQUEST_TIMEOUT_SECONDS";

    @Mock
    private RestTemplateBuilder mockRestTemplateBuilder;

    @Mock
    private EnvironmentReader environmentReader;

    @InjectMocks
    private SpringConfiguration springConfiguration;

    @Captor
    private ArgumentCaptor<Duration> timeout;

    @Test
    void testReadTimeoutEnvVariableUsed() {
        //given
        Duration expected = Duration.ofSeconds(MOCK_TIMEOUT_SECONDS);
        doReturn(MOCK_TIMEOUT_SECONDS).when(environmentReader).getOptionalInteger(OCR_REQUEST_TIMEOUT_SECONDS);
        doReturn(mockRestTemplateBuilder).when(mockRestTemplateBuilder)
                .setReadTimeout(Duration.ofSeconds(MOCK_TIMEOUT_SECONDS));
        doReturn(mockRestTemplateBuilder).when(mockRestTemplateBuilder)
                .setConnectTimeout(Duration.ofSeconds(MOCK_TIMEOUT_SECONDS));
        doReturn(new RestTemplate()).when(mockRestTemplateBuilder).build();

        // when
        RestTemplate restTemplate = springConfiguration.restTemplate(mockRestTemplateBuilder, environmentReader);

        // then
        verify(environmentReader, times(1)).getOptionalInteger(OCR_REQUEST_TIMEOUT_SECONDS);
        verify(mockRestTemplateBuilder).setReadTimeout(timeout.capture());
        Duration actual = timeout.getValue();
        assertThat(actual, is(expected));

    }

    @Test
    void testReadTimeoutDefaultUsed() {
        // given
        Duration expected = Duration.ofSeconds(DEFAULT_REQUEST_TIMEOUT_SECONDS);
        doReturn(null).when(environmentReader).getOptionalInteger(OCR_REQUEST_TIMEOUT_SECONDS);
        doReturn(mockRestTemplateBuilder).when(mockRestTemplateBuilder)
                .setReadTimeout(Duration.ofSeconds(DEFAULT_REQUEST_TIMEOUT_SECONDS));
        doReturn(mockRestTemplateBuilder).when(mockRestTemplateBuilder)
                .setConnectTimeout(Duration.ofSeconds(DEFAULT_REQUEST_TIMEOUT_SECONDS));
        doReturn(new RestTemplate()).when(mockRestTemplateBuilder).build();

        // when
        RestTemplate restTemplate = springConfiguration.restTemplate(mockRestTemplateBuilder, environmentReader);

        // then
        verify(mockRestTemplateBuilder).setReadTimeout(timeout.capture());
        verify(environmentReader, times(1)).getOptionalInteger(OCR_REQUEST_TIMEOUT_SECONDS);
        Duration actual = timeout.getValue();
        assertThat(actual, is(expected));

    }

}
