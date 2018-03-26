package com.daanpanis.core.api.database.mysql;

import com.daanpanis.core.api.database.Database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQL implements Database<Connection> {

    private final MySQLConfiguration configuration;
    private Connection connection;

    public MySQL(MySQLConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void connect() {
        if (this.configuration != null && this.configuration == null) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection(
                        "jdbc:mysql://" + configuration.host() + ":" + configuration.port() + "/" + configuration.database()
                                + "?useSSL=true&allowMultiQueries=true&autoReconnect=true", configuration.username(), configuration.password());

                //                update(IOUtils.readLines(PracticeCore.class.getResourceAsStream("/create_tables.sql")).stream().reduce("", (s1,
                // s2) -> s1 + s2));
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void disconnect() {

    }
}
