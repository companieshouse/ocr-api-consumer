package uk.gov.companieshouse.ocrapiconsumer.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static uk.gov.companieshouse.ocrapiconsumer.configuration.SpringConfiguration.DEFAULT_REQUEST_TIMEOUT_SECONDS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@ExtendWith(MockitoExtension.class)
class RestTemplateTimeoutConfigurationTest {

    private static final Integer MOCK_TIMEOUT_SECONDS = 60;

    @Mock
    private RestTemplateBuilder mockRestTemplateBuilder;

    @InjectMocks
    private SpringConfiguration springConfiguration;

    @Captor
    private ArgumentCaptor<Duration> timeout;

    @Test
    void testReadTimeoutEnvVariableUsed() {
        //given
        Duration expected = Duration.ofSeconds(MOCK_TIMEOUT_SECONDS);
        springConfiguration.ocrRequestTimeoutSeconds = MOCK_TIMEOUT_SECONDS;
        doReturn(mockRestTemplateBuilder).when(mockRestTemplateBuilder)
                .setReadTimeout(Duration.ofSeconds(MOCK_TIMEOUT_SECONDS));
        doReturn(mockRestTemplateBuilder).when(mockRestTemplateBuilder)
                .setConnectTimeout(Duration.ofSeconds(MOCK_TIMEOUT_SECONDS));
        doReturn(new RestTemplate()).when(mockRestTemplateBuilder).build();

        // when
       springConfiguration.restTemplate(mockRestTemplateBuilder);

        verify(mockRestTemplateBuilder).setReadTimeout(timeout.capture());
        Duration actual = timeout.getValue();
        assertThat(actual, is(expected));

    }

    @Test
    void testReadTimeoutDefaultUsed() {
        // given
        Duration expected = Duration.ofSeconds(DEFAULT_REQUEST_TIMEOUT_SECONDS);
        doReturn(mockRestTemplateBuilder).when(mockRestTemplateBuilder)
                .setReadTimeout(Duration.ofSeconds(DEFAULT_REQUEST_TIMEOUT_SECONDS));
        doReturn(mockRestTemplateBuilder).when(mockRestTemplateBuilder)
                .setConnectTimeout(Duration.ofSeconds(DEFAULT_REQUEST_TIMEOUT_SECONDS));
        doReturn(new RestTemplate()).when(mockRestTemplateBuilder).build();

        // when
        springConfiguration.restTemplate(mockRestTemplateBuilder);

        // then
        verify(mockRestTemplateBuilder).setReadTimeout(timeout.capture());
        Duration actual = timeout.getValue();
        assertThat(actual, is(expected));

    }

}
