package com.daanpanis.core.api.command.parsers;

import com.daanpanis.core.api.command.CommandArgument;
import com.daanpanis.core.api.command.ParameterParser;
import com.daanpanis.core.api.command.exceptions.CommandExecutionException;

public class IntegerParser implements ParameterParser<Integer> {

    @Override
    public Integer parse(CommandArgument argument, String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new CommandExecutionException(argument.getName() + " must be a number!");
        }
    }
}
