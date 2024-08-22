package com.dsi.exchange;

import com.dsi.ConnectionManager;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class TopicExchange {

    private static final String HEALTH_Q = "q_health_t";
    private static final String SPORTS_Q = "q_sports_t";
    private static final String EDUCATION_Q = "q_education_t";

    private static final String EXCHANGE_NAME = "ex_home_topic";

    private static final String ROUTING_KEY_HEALTH = "health.*";
    private static final String ROUTING_KEY_SPORTS = "#.sports.*";
    private static final String ROUTING_KEY_EDUCATION = "#.education";

    public static void main(String[] args) throws IOException, TimeoutException {
        TopicExchange.declareQueues();
        TopicExchange.declareExchanges();
        TopicExchange.declareBindings();

        Thread publish = new Thread(() -> {
            try {
                TopicExchange.publishMessages();
            } catch (IOException | TimeoutException e) {
                System.err.println(e.getMessage());
            }
        });

        Thread consume = new Thread(() -> {
            try {
                Thread.sleep(10 * 1000); // waiting for 10 s
                TopicExchange.consumeMessages();
            } catch (IOException | TimeoutException | InterruptedException e) {
                System.err.println(e.getMessage());
            }
        });

        publish.start();
        consume.start();
    }

    private static void declareQueues() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();

        channel.queueDeclare(HEALTH_Q, false, false, false, null);
        System.out.println("Queue '" + HEALTH_Q + "' Created.");

        channel.queueDeclare(SPORTS_Q, false, false, false, null);
        System.out.println("Queue '" + SPORTS_Q + "' Created.");

        channel.queueDeclare(EDUCATION_Q, false, false, false, null);
        System.out.println("Queue '" + EDUCATION_Q + "' Created.");
    }

    private static void declareExchanges() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC, true);
        System.out.println("TopicExchange '" + EXCHANGE_NAME + "' Created.");
    }

    private static void declareBindings() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();

        channel.queueBind(HEALTH_Q, EXCHANGE_NAME, ROUTING_KEY_HEALTH);
        System.out.println("Queue '" + HEALTH_Q + "' bound with exchange '" + EXCHANGE_NAME + "' via routing key '" + ROUTING_KEY_HEALTH + "'.");

        channel.queueBind(SPORTS_Q, EXCHANGE_NAME, ROUTING_KEY_SPORTS);
        System.out.println("Queue '" + SPORTS_Q + "' bound with exchange '" + EXCHANGE_NAME + "' via routing key '" + ROUTING_KEY_SPORTS + "'.");

        channel.queueBind(EDUCATION_Q, EXCHANGE_NAME, ROUTING_KEY_EDUCATION);
        System.out.println("Queue '" + EDUCATION_Q + "' bound with exchange '" + EXCHANGE_NAME + "' via routing key '" + ROUTING_KEY_EDUCATION + "'.");
    }

    private static void publishMessages() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();

        System.out.println("[*] Waiting for publish. To exit press CTRL+C");

        String message = "Drink a lot of water and stay healthy.";
        channel.basicPublish(EXCHANGE_NAME, "health.education", null, message.getBytes(StandardCharsets.UTF_8));
        System.out.println("[x] Sent '" + message + "'");

        message = "Learn something new everyday.";
        channel.basicPublish(EXCHANGE_NAME, "education", null, message.getBytes(StandardCharsets.UTF_8));
        System.out.println("[x] Sent '" + message + "'");

        message = "Stay fit in mind and body";
        channel.basicPublish(EXCHANGE_NAME, "education.health", null, message.getBytes(StandardCharsets.UTF_8));
        System.out.println("[x] Sent '" + message + "'");
    }

    private static void consumeMessages() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();

        System.out.println("[*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println("[*] Received '" + message + "'");
        };

        CancelCallback cancelCallback = (consumerTag) -> {
            System.out.println("[x] Cancelled '" + consumerTag + "'");
        };

        channel.basicConsume(HEALTH_Q, true, deliverCallback, cancelCallback);
        channel.basicConsume(SPORTS_Q, true, deliverCallback, cancelCallback);
        channel.basicConsume(EDUCATION_Q, true, deliverCallback, cancelCallback);
    }
}