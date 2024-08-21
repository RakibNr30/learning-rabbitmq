package com.dsi;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Consumer {
    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = RabbitMQUtils.getConnectionFactory();

        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            System.out.println("[*] Waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println("[x] Received '" + message + "'");
            };

            CancelCallback cancelCallback = (consumerTag) -> {
                System.out.println("[x] Cancelled '" + consumerTag + "'");
            };

            channel.basicConsume(RabbitMQUtils.getDefaultQueue(), true, deliverCallback, cancelCallback);

            synchronized (Consumer.class) {
                try {
                    Consumer.class.wait(); // Wait indefinitely
                } catch (InterruptedException e) {
                    System.out.println("Consumer was interrupted.");
                }
            }
        }
    }
}