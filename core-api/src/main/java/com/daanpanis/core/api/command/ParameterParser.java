package com.daanpanis.core.api.command;

public interface ParameterParser<T> {

    T parse(CommandArgument argument, String value);

}
