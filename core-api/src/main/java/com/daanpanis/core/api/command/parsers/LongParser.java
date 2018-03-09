package com.daanpanis.core.api.command.parsers;

import com.daanpanis.core.api.command.CommandArgument;
import com.daanpanis.core.api.command.ParameterParser;
import com.daanpanis.core.api.command.exceptions.CommandExecutionException;

public class LongParser implements ParameterParser<Long> {

    @Override
    public Long parse(CommandArgument argument, String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new CommandExecutionException(argument.getName() + " must be a number!");
        }
    }
}
