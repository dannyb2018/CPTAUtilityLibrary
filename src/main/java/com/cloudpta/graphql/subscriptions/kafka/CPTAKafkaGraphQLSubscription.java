package com.cloudpta.graphql.subscriptions.kafka;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import com.cloudpta.graphql.common.CPTAGraphQLInput;
import com.cloudpta.graphql.common.CPTAGraphQLAPIConstants;
import com.cloudpta.graphql.subscriptions.CPTAGraphQLSubscription;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

public abstract class CPTAKafkaGraphQLSubscription<ResultType,RequestType extends CPTAGraphQLInput> extends CPTAGraphQLSubscription<ResultType,RequestType> 
{
    @Override
    protected void setupSource()
    {
        
        // Need to get bootstap broker urls from context
        String url = context.get(CPTAGraphQLAPIConstants.KAFKA_BOOTSTRAP_BROKERS_URL);
        // Get the group id
        String groupID = context.get(CPTAGraphQLAPIConstants.KAFKA_GROUP_ID);
        // Where to start from
        String offsetReset = context.get(CPTAGraphQLAPIConstants.KAFKA_OFFSET_RESET);        
        // Get the text of the schema to use
        String schemaToUse = context.get(CPTAGraphQLAPIConstants.KAFKA_SCHEMA_TO_USE);        

        Properties props = new Properties();
        props.setProperty("bootstrap.servers", url);
        
        // group id is the rfq id
        props.setProperty("group.id", groupID);
        // Auto commit to false
        props.setProperty("enable.auto.commit", "false");
        props.setProperty("auto.offset.reset", offsetReset);
        props.setProperty("max.poll.records", Integer.toString(10000));
        
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");

        // Get the names of topics to read from, comma delimited
        topics = new ArrayList<>();
        String topicsToBrowseAsCommaDelimitedString = context.get(CPTAGraphQLAPIConstants.KAFKA_TOPIC_TO_BROWSE);
        String[] topicsAsArray = topicsToBrowseAsCommaDelimitedString.split(",");
        int numberOfTopics = Array.getLength(topicsAsArray);
        for(int i = 0; i < numberOfTopics; i++)
        {
            // add the topic
            topics.add(topicsAsArray[i]);
        }

        // create consumer
        consumer = new KafkaConsumer<>(props); 
        
        // Set up schema parser
        schema = new Schema.Parser().parse(schemaToUse);
        datumReader = new GenericDatumReader<GenericRecord>(schema);
    }

    @Override
    protected void subscribeToSource()
    {
        consumer.subscribe(topics);        
    }

    @Override
    protected void unsubscribeFromSource()
    {
        consumer.close();        
    }

    @Override
    protected List<JsonObject> readFromSource(long timeout) throws IOException
    {
        List<JsonObject> jsonReadFromKafka = new ArrayList<>();

        ConsumerRecords<byte[], byte[]> recordsReadFromKafka = consumer.poll(Duration.ofMillis(timeout));

        for(ConsumerRecord<byte[], byte[]> currentRecord : recordsReadFromKafka)
        {
            // Convert to Json
            JsonObject recordAsJson = convertFromKafkaRecord(currentRecord);
            // Add to list
            jsonReadFromKafka.add(recordAsJson);
        }

        return jsonReadFromKafka;
    } 

    protected JsonObject convertFromKafkaRecord(ConsumerRecord<byte[], byte[]> kafkaRecord) throws IOException
    {
        
        byte[] encodedRecordAsByteArray = kafkaRecord.value();
        // Get the decoder to json
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(encodedRecordAsByteArray, null);
        GenericRecord decodedValue = datumReader.read(null, decoder);
        // Get the result
        String decodedAvroAsJsonString = decodedValue.toString();

        // Turn into JsonObject
        JsonReader reader = Json.createReader(new StringReader(decodedAvroAsJsonString));
        JsonObject recordAsJson = reader.readObject();

        return recordAsJson;
    }

    protected Schema schema;
    protected GenericDatumReader<GenericRecord> datumReader;
    protected KafkaConsumer<byte[], byte[]> consumer = null;
    protected List<String> topics = null;
}
