package com.daanpanis.core.api.database;

public interface Database<T> {

    T getConnection();

    void connect();

    void disconnect();

}
