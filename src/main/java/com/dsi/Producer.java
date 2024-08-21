package com.dsi;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Producer {

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = RabbitMQUtils.getConnectionFactory();

        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.queueDeclare(RabbitMQUtils.getDefaultQueue(), false, false, false, null);

            for (int i = 1; i <= 10; i++) {
                String message = "Test Message via RabbitMQ - " + i;
                channel.basicPublish("", RabbitMQUtils.getDefaultQueue(), null, message.getBytes(StandardCharsets.UTF_8));
                System.out.println("[x] Sent '" + message + "'");
            }
        }
    }
}