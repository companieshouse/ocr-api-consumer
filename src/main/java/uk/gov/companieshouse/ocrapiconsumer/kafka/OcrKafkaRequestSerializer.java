// package uk.gov.companieshouse.ocrapiconsumer.kafka;

// import org.apache.avro.generic.IndexedRecord;
// import org.apache.avro.io.DatumReader;
// import org.apache.avro.io.Decoder;
// import org.apache.avro.io.DecoderFactory;
// import org.apache.avro.reflect.ReflectDatumReader;
// import org.apache.kafka.common.errors.SerializationException;
// import org.apache.kafka.common.serialization.Serializer;
// import org.springframework.stereotype.Component;
// import uk.gov.companieshouse.ocrapiconsumer.request.OcrKafkaRequest;

// import java.util.Arrays;
// import java.util.Map;

// @Component
// public class OcrKafkaRequestSerializer<T extends IndexedRecord> implements Serializer<T> {
    
//     @SuppressWarnings("unchecked")
//     @Override
//     public T serialize(String topic, byte[] data) {

//         try {
//             Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
//             DatumReader<OcrKafkaRequest> reader = new ReflectDatumReader<>(OcrKafkaRequest.class);

//             return (T)reader.read(null, decoder);
            
//         } catch (Exception e) {
//             throw new SerializationException(
//                     "Message data [" + Arrays.toString(data) + "] from topic [" + topic + "] cannot be deserialized", e);
//         }
//     }

//     @Override
//     public byte[] serialize(String topic, T data) {
        
//         try {

//         } catch (Exception e) {
//             throw new SerializationException(
//                     "Message data [" + Arrays.toString(data) + "] from topic [" + topic + "] cannot be serialized", e);
//         }
//     }

//     @Override
//     public void close() {
//         // No-op
//     }

//     @Override
//     public void configure(Map<String, ?> arg0, boolean arg1) {
//         // No-op
//     }
// }
