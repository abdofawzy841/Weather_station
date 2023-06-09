package com.weather;

import java.time.Duration;
import java.util.*;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeClusterResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Consumer {
    KafkaConsumer<String, String> consumer;
    public Consumer(String topic){
        String bootstrapServers = "kafka:9092";
        String groupId = "test-consumer-group";

        // create consumer configs
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        AdminClient adminClient = AdminClient.create(properties);
        while (true) {
            try {
                DescribeClusterResult describeClusterResult = adminClient.describeCluster();
                describeClusterResult.clusterId().get();
                System.out.println("Connection to Kafka established successfully.");
                break;
            } catch (Exception e) {
                System.out.println("Attempt to connect to Kafka failed. Retrying in 1000 ms.");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        try {
            consumer = new KafkaConsumer<>(properties);
            consumer.subscribe(Collections.singleton(topic));
        }catch(Exception e){
            System.out.println("Topic: " + topic);
            e.printStackTrace();
        }
    }
    public List<Status> poll(){
            ConsumerRecords<String, String> records =
                    consumer.poll(Duration.ofMillis(1000));
            List<Status> values = new LinkedList<>();
            ObjectMapper mapper = new ObjectMapper();
            for (ConsumerRecord<String, String> record : records){
                try {
                    values.add(mapper.readValue(record.value(), Status.class));
                } catch (JsonProcessingException e) {
                    System.out.println("Failed to parse : ");
                    System.out.println(record.value());
                    e.printStackTrace();
                }
            }
            return values;
    }
}
