////////////////////////////////////////////////////////////////////////////////
//
//                                 NOTICE:
//  THIS PROGRAM CONSISTS OF TRADE SECRECTS THAT ARE THE PROPERTY OF
//  Advanced Products Ltd. THE CONTENTS MAY NOT BE USED OR DISCLOSED
//  WITHOUT THE EXPRESS WRITTEN PERMISSION OF THE OWNER.
//
//               COPYRIGHT Advanced Products Ltd 2016-2019
//
////////////////////////////////////////////////////////////////////////////////
package com.cloudpta.graphql.subscriptions.kafka;

import java.io.IOException;
import java.io.StringReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import com.cloudpta.graphql.subscriptions.QPSubscriptionFeedPublisher;
import com.cloudpta.graphql.common.QPGraphQLAPIConstants;
import com.cloudpta.graphql.common.QPGraphQLInput;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

public abstract class QPKafkaSubscriptionFeedPublisher<ResultType,RequestType extends QPGraphQLInput> extends QPSubscriptionFeedPublisher<ResultType,RequestType> 
{
    @Override
    protected void setupSource()
    {
        // Need to get bootstap broker urls from context
        String url = context.get(QPGraphQLAPIConstants.KAFKA_BOOTSTRAP_BROKERS_URL);
        // Get the group id
        String groupID = context.get(QPGraphQLAPIConstants.KAFKA_GROUP_ID);
        // Where to start from
        String offsetReset = context.get(QPGraphQLAPIConstants.KAFKA_OFFSET_RESET);
        // Get the name of the topic to read from
        String topicToBrowse = context.get(QPGraphQLAPIConstants.KAFKA_TOPIC_TO_BROWSE);
        // Get the text of the schema to use
        String schemaToUse = context.get(QPGraphQLAPIConstants.KAFKA_SCHEMA_TO_USE);

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

        topics.add(topicToBrowse);
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
            JsonObject quoteAsJson = convertFromKafkaRecord(currentRecord);
            // Add to list
            jsonReadFromKafka.add(quoteAsJson);
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
        JsonObject quoteAsJson = reader.readObject();

        return quoteAsJson;
    }

    protected Schema schema;
    protected GenericDatumReader<GenericRecord> datumReader;
    protected KafkaConsumer<byte[], byte[]> consumer = null;
    protected List<String> topics = new ArrayList<>();
}
