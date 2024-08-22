package com.dsi.exchange;

import com.dsi.ConnectionManager;
import com.dsi.Constants;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class DirectExchange {

    public static void main(String[] args) throws IOException, TimeoutException {
        DirectExchange.declareQueues();
        DirectExchange.declareExchanges();
        DirectExchange.declareBindings();

        Thread publish = new Thread(() -> {
            try {
                DirectExchange.publishMessages();
            } catch (IOException | TimeoutException e) {
                System.err.println(e.getMessage());
            }
        });

        Thread consume = new Thread(() -> {
            try {
                Thread.sleep(10 * 1000); // waiting for 10 s
                DirectExchange.consumeMessages();
            } catch (IOException | TimeoutException | InterruptedException e) {
                System.err.println(e.getMessage());
            }
        });

        publish.start();
        consume.start();
    }

    private static void declareQueues() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();

        channel.queueDeclare(Constants.MOBILE_Q, false, false, false, null);
        System.out.println("Queue '" + Constants.MOBILE_Q + "' Created.");

        channel.queueDeclare(Constants.FAN_Q, false, false, false, null);
        System.out.println("Queue '" + Constants.FAN_Q + "' Created.");

        channel.queueDeclare(Constants.LIGHT_Q, false, false, false, null);
        System.out.println("Queue '" + Constants.LIGHT_Q + "' Created.");
    }

    private static void declareExchanges() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();
        channel.exchangeDeclare(Constants.EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true);
        System.out.println("DirectExchange '" + Constants.EXCHANGE_NAME + "' Created.");
    }

    private static void declareBindings() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();

        channel.queueBind(Constants.MOBILE_Q, Constants.EXCHANGE_NAME, Constants.ROUTING_KEY_PD);
        System.out.println("Queue '" + Constants.MOBILE_Q + "' bound with exchange '" + Constants.EXCHANGE_NAME + "' via routing key '" + Constants.ROUTING_KEY_PD + "'.");

        channel.queueBind(Constants.FAN_Q, Constants.EXCHANGE_NAME, Constants.ROUTING_KEY_HA);
        System.out.println("Queue '" + Constants.FAN_Q + "' bound with exchange '" + Constants.EXCHANGE_NAME + "' via routing key '" + Constants.ROUTING_KEY_HA + "'.");

        channel.queueBind(Constants.LIGHT_Q, Constants.EXCHANGE_NAME, Constants.ROUTING_KEY_HA);
        System.out.println("Queue '" + Constants.LIGHT_Q + "' bound with exchange '" + Constants.EXCHANGE_NAME + "' via routing key '" + Constants.ROUTING_KEY_HA + "'.");
    }

    private static void publishMessages() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();

        System.out.println("[*] Waiting for publish. To exit press CTRL+C");

        channel.basicPublish(Constants.EXCHANGE_NAME, Constants.ROUTING_KEY_HA, null, "Turn on Home Appliances.".getBytes(StandardCharsets.UTF_8));
        System.out.println("[x] Sent 'Turn on Home Appliances.'");
        channel.basicPublish(Constants.EXCHANGE_NAME, Constants.ROUTING_KEY_PD, null, "Turn off Personal Devices.".getBytes(StandardCharsets.UTF_8));
        System.out.println("[x] Sent 'Turn off Personal Devices.'");
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

        channel.basicConsume(Constants.MOBILE_Q, true, deliverCallback, cancelCallback);
        channel.basicConsume(Constants.FAN_Q, true, deliverCallback, cancelCallback);
        channel.basicConsume(Constants.LIGHT_Q, true, deliverCallback, cancelCallback);
    }
}