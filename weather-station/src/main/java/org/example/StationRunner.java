package org.example;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.*;

import java.io.IOException;
import java.util.*;


public class StationRunner {

    public static void main(String[] args) throws IOException {

        final String BOOTSTRAP_SERVERS = "localhost:9092";
        final String TOPIC_NAME = "weather-topic";
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        try {
            Thread[] stationThreads = new Thread[10];
            for (int i = 0; i < stationThreads.length; i++) {
                stationThreads[i] = new Thread(new Station(i + 1, 3498, producerProps, TOPIC_NAME));
                stationThreads[i].start();
            }
            for (int i = 0; i < stationThreads.length; i++) {
                stationThreads[i].join();
            }
        } catch (InterruptedException e) {
            System.err.println("Client interrupted");
            e.printStackTrace();
        }
    }
}
