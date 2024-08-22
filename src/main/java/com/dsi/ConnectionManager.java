package com.dsi;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public abstract class ConnectionManager {
    private static Connection connection;

    private static final String HOST = "127.0.0.1";
    private static final Integer PORT = 5672;

    public static Connection getConnection() throws IOException, TimeoutException {
        if (connection == null) {
            connection = getConnectionFactory().newConnection();
        }

        return connection;
    }

    private static ConnectionFactory getConnectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(ConnectionManager.HOST);
        factory.setPort(ConnectionManager.PORT);

        return factory;
    }
}
