package com.dsi;

import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQUtils {
    private static ConnectionFactory factory;

    private static final String HOST = "127.0.0.1";
    private static final Integer PORT = 5672;
    private static final String DEFAULT_QUEUE = "Default-Queue";

    public static String getDefaultQueue() {
        return DEFAULT_QUEUE;
    }

    public static ConnectionFactory getConnectionFactory() {
        if (factory == null) {
            factory = new ConnectionFactory();
            factory.setHost(RabbitMQUtils.HOST);
            factory.setPort(RabbitMQUtils.PORT);
        }

        return factory;
    }
}
