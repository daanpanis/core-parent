package com.daanpanis.core.api.database.mysql;

import org.bukkit.configuration.ConfigurationSection;

public class MySQLConfiguration {

    private String host = "localhost";
    private int port = 3306;
    private String username = "username";
    private String password;
    private String database = "database";

    public MySQLConfiguration() {
    }

    public MySQLConfiguration(ConfigurationSection section) {
        if (section != null) {
            this.host = section.getString("host", "localhost");
            this.port = section.getInt("port", 3306);
            this.username = section.getString("username", "username");
            this.password = section.getString("password");
            this.database = section.getString("database", "database");
        }
    }

    public String host() {
        return host;
    }

    public MySQLConfiguration host(String host) {
        this.host = host;
        return this;
    }

    public int port() {
        return this.port;
    }

    public MySQLConfiguration port(int port) {
        this.port = port;
        return this;
    }

    public String username() {
        return this.username;
    }

    public MySQLConfiguration username(String username) {
        this.username = username;
        return this;
    }

    public String password() {
        return password;
    }

    public MySQLConfiguration password(String password) {
        this.password = password;
        return this;
    }

    public String database() {
        return database;
    }

    public MySQLConfiguration database(String database) {
        this.database = database;
        return this;
    }

}
