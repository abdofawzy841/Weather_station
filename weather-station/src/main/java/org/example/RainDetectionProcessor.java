package org.example;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;


import java.util.Properties;

public class RainDetectionProcessor {
    private static final int HUMIDITY_THRESHOLD = 70;

    public static void main(String[] args) {
        // Set up Kafka Streams configuration
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "rain-detection-processor");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());


        // Create the StreamsBuilder
        StreamsBuilder builder = new StreamsBuilder();

        // Create the input stream from the weather data topic
        KStream<String, String> weatherDataStream = builder.stream("weather-topic");

        // Process the weather data stream
        weatherDataStream.filter((key, value) -> isHumidityAboveThreshold(value))
                .mapValues(value ->{
                    System.out.println("It's raining! Humidity above 70%.: " + value);
                    return value;
                })
                .to("rain_trigger");

        // Build the Kafka Streams application
        KafkaStreams streams = new KafkaStreams(builder.build(), props);

        // Start the Kafka Streams application
        streams.start();

        // Add shutdown hook to gracefully close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private static boolean isHumidityAboveThreshold(String weatherData) {

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(weatherData, JsonObject.class);

        int humidityValue = jsonObject.getAsJsonObject("weather").get("humidity").getAsInt();

        return humidityValue > HUMIDITY_THRESHOLD;
    }
}
