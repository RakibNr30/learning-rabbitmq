package com.dsi.exchange;

import com.dsi.ConnectionManager;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class FanoutExchange {

    private static final String MOBILE_Q = "q_mobile_fo";
    private static final String FAN_Q = "q_fan_fo";
    private static final String LIGHT_Q = "q_light_fo";

    private static final String EXCHANGE_NAME = "ex_home_fanout";

    public static void main(String[] args) throws IOException, TimeoutException {
        FanoutExchange.declareQueues();
        FanoutExchange.declareExchanges();
        FanoutExchange.declareBindings();

        Thread publish = new Thread(() -> {
            try {
                FanoutExchange.publishMessages();
            } catch (IOException | TimeoutException e) {
                System.err.println(e.getMessage());
            }
        });

        Thread consume = new Thread(() -> {
            try {
                Thread.sleep(10 * 1000); // waiting for 10 s
                FanoutExchange.consumeMessages();
            } catch (IOException | TimeoutException | InterruptedException e) {
                System.err.println(e.getMessage());
            }
        });

        publish.start();
        consume.start();
    }

    private static void declareQueues() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();

        channel.queueDeclare(MOBILE_Q, false, false, false, null);
        System.out.println("Queue '" + MOBILE_Q + "' Created.");

        channel.queueDeclare(FAN_Q, false, false, false, null);
        System.out.println("Queue '" + FAN_Q + "' Created.");

        channel.queueDeclare(LIGHT_Q, false, false, false, null);
        System.out.println("Queue '" + LIGHT_Q + "' Created.");
    }

    private static void declareExchanges() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT, true);
        System.out.println("FanoutExchange '" + EXCHANGE_NAME + "' Created.");
    }

    private static void declareBindings() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();

        channel.queueBind(MOBILE_Q, EXCHANGE_NAME, "");
        System.out.println("Queue '" + MOBILE_Q + "' bound with exchange '" + EXCHANGE_NAME + ".");

        channel.queueBind(FAN_Q, EXCHANGE_NAME, "");
        System.out.println("Queue '" + FAN_Q + "' bound with exchange '" + EXCHANGE_NAME + ".");

        channel.queueBind(LIGHT_Q, EXCHANGE_NAME, "");
        System.out.println("Queue '" + LIGHT_Q + "' bound with exchange '" + EXCHANGE_NAME + ".");
    }

    private static void publishMessages() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();

        System.out.println("[*] Waiting for publish. To exit press CTRL+C");

        String message = "Turn all devices.";
        channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes(StandardCharsets.UTF_8));
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

        channel.basicConsume(MOBILE_Q, true, deliverCallback, cancelCallback);
        channel.basicConsume(FAN_Q, true, deliverCallback, cancelCallback);
        channel.basicConsume(LIGHT_Q, true, deliverCallback, cancelCallback);
    }
}