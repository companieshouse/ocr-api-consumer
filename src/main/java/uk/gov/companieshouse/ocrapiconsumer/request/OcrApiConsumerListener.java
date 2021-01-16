package uk.gov.companieshouse.ocrapiconsumer.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.ocrapiconsumer.OcrApiConsumerApplication;

@Component
public class OcrApiConsumerListener {

    private static final Logger LOG = LoggerFactory.getLogger(OcrApiConsumerApplication.APPLICATION_NAME_SPACE);

    private final OcrApiConsumerService ocrApiConsumerService;

    @Autowired
    public OcrApiConsumerListener(final OcrApiConsumerService ocrApiConsumerService) {
        this.ocrApiConsumerService = ocrApiConsumerService;
    }

    @KafkaListener(topics = "tutorialspoint", groupId = "group-id")
    public void listen(String responseId) {
        LOG.debug("Received Messasge in group - group-id: " + responseId);

        boolean doKafka = false;
        if (doKafka) {
            ocrApiConsumerService.sendOcrApiRequest(responseId);
        }
    }
}
