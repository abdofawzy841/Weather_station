package org.example;

import java.util.*;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;


public class Station extends Thread {

  private KafkaProducer<String,String> producer;
  private MessageGenerator messageGenerator;
  private String topicName;
  private Random droppingRandom;

  public Station(long stationID, long startingSequenceNumber, Properties producerProps, String topicName) {
    this.messageGenerator = new MessageGenerator(stationID, startingSequenceNumber);    
    this.producer = new KafkaProducer<>(producerProps);
    this.topicName  = topicName;
    this.droppingRandom = new Random();
  }

  public void run() {
    while (true) {
      long startTime = System.currentTimeMillis();
      String message = messageGenerator.getNextMessage();
      if (droppingRandom.nextDouble() > 0.1 ) {
        producer.send(new ProducerRecord<String,String>(topicName, message));
      }
      long timeTaken = System.currentTimeMillis() - startTime;
      try {
        Thread.sleep((1000 - timeTaken) > 0 ? 1000 - timeTaken : 0);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

}
