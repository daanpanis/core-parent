package com.daanpanis.core.api.command.parsers;

import com.daanpanis.core.api.command.CommandArgument;
import com.daanpanis.core.api.command.ParameterParser;

public class StringParser implements ParameterParser<String> {

    @Override
    public String parse(CommandArgument argument, String value) {
        return value;
    }
}
