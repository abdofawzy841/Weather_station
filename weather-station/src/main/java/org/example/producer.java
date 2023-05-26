package org.example;
import org.json.JSONObject;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class producer {
    static Random random = new Random();
    public static int low=3;
    public static int message_num=10;
    public static int high=3;
    public static int dropedNum = random.nextInt(9) +1;
    public static int medium=4;
    public static long s_n=1;
    static Object lock = new Object();
    static Object resetlock = new Object();

    public static JSONObject getJsonObjectById(long station_id) {
        String battery_status = "";

        while(true) {
            int randomNumber = random.nextInt(3);
            synchronized (lock){

                if (randomNumber == 0 && low > 0) {
                    battery_status = "low";
                    low--;

                    break;
                } else if (randomNumber == 1 && medium > 0) {
                    battery_status = "medium";
                    medium--;

                    break;
                } else if (randomNumber == 2 && high > 0) {
                    battery_status = "high";
                    high--;

                    break;
            }}
        }
        long Time = System.currentTimeMillis();
        JSONObject jsonObject_weather = new JSONObject();
        int random_humidity = random.nextInt(100);
        jsonObject_weather.put("humidity", random_humidity);
        jsonObject_weather.put("temperature", 100);
        jsonObject_weather.put("wind_speed",13);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("station_id", station_id);
        jsonObject.put("s_no", s_n);
        jsonObject.put("battery_status",battery_status);
        jsonObject.put("status_timestamp", Time);
        jsonObject.put("weather", jsonObject_weather);

        System.out.println(jsonObject);
        return jsonObject;
    }

    public static void main(String[] args) throws IOException {

        final String BOOTSTRAP_SERVERS = "localhost:9092";
        final String TOPIC_NAME = "weather-topic";
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String,String> [] producers = new KafkaProducer[10];
        for ( int i=0;i<10;i++ ){
            producers[i] = new KafkaProducer<>(producerProps);
        }




        // Schedule tasks to run after a delay
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
         dropedNum = random.nextInt(9) +1;
        for (int i = 1; i <= 10; i++) {
            final long taskNumber = (long) i;
            final int task = i-1;

            executor.scheduleAtFixedRate(() -> {
                if(task+1 != dropedNum) {
                    ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, getJsonObjectById(taskNumber).toString());
                    producers[task].send(record);
                }
                synchronized (resetlock) {

                message_num--;
                if(message_num==0){
                dropedNum = random.nextInt(9) +1;
                s_n++;
                low=3;
                high=3;
                medium=4;
                message_num=10;
                    System.out.println("round"+ s_n );
                }}
            }, 0,1, TimeUnit.SECONDS); // Delay of 1 second

        }

    }
}