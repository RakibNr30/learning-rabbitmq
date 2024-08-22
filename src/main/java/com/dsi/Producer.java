package com.dsi;

import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Producer {

    public static void main(String[] args) throws IOException, TimeoutException {

        try (Channel channel = ConnectionManager.getConnection().createChannel()) {
            channel.queueDeclare(Constants.DEFAULT_QUEUE, false, false, false, null);

            for (int i = 1; i <= 10; i++) {
                String message = "Test Message via RabbitMQ - " + i;
                channel.basicPublish("", Constants.DEFAULT_QUEUE, null, message.getBytes(StandardCharsets.UTF_8));
                System.out.println("[x] Sent '" + message + "'");
            }
        }
    }
}