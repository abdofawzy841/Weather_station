package org.example;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.*;

import org.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
public class consumer {
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String TOPIC_NAME = "weather-topic";
    private static final String GROUP_ID = "test-consumer-group";

    public static void main(String[] args) {
        // Create Kafka consumer properties
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());


        final String BOOTSTRAP_SERVERS = "localhost:9092";
        final String Produc_TOPIC_NAME = "rain_trigger";
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Create Kafka consumer instance
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);

        // Subscribe to the topic
        consumer.subscribe(Collections.singleton(TOPIC_NAME));

        // Start consuming messages
        int counter=1;
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                Gson gson = new Gson();
                for (ConsumerRecord<String, String> record : records) {
                    // Convert the record value to a JsonObject
                    JsonObject jsonObject = gson.fromJson(record.value(), JsonObject.class);

                    // Now you can work with the JsonObject
                    // For example, you can access its properties
                    String propertyValue = jsonObject.getAsJsonObject("weather").get("humidity").getAsString();
                    System.out.println("message # :"+counter++);
                    System.out.println("humidity message: " + propertyValue);



                    if( Integer.parseInt(propertyValue)>70){
                        ProducerRecord<String, String> produc_record = new ProducerRecord<>(Produc_TOPIC_NAME, record.value());
                        producer.send(produc_record);
                        System.out.println("rain trigger message: "+record.value());

                    }
                }
            }
        } finally {
            consumer.close();
        }
    }}
