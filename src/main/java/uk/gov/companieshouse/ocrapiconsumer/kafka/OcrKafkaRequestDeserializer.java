package uk.gov.companieshouse.ocrapiconsumer.kafka;

import java.util.Arrays;

import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.ocr.OcrRequestMessage;

@Component
public class OcrKafkaRequestDeserializer<T extends IndexedRecord> implements Deserializer<T> {

    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(String topic, byte[] data) {

        try {
            Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            DatumReader<OcrRequestMessage> reader = new ReflectDatumReader<>(OcrRequestMessage.class);

            return (T)reader.read(null, decoder);
            
        } catch (Exception e) {
            throw new SerializationException(
                    "Message data [" + Arrays.toString(data) + "] from topic [" + topic + "] cannot be deserialized", e);
        }
    }

}
