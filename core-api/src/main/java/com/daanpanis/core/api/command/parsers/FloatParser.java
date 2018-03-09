package com.daanpanis.core.api.command.parsers;

import com.daanpanis.core.api.command.CommandArgument;
import com.daanpanis.core.api.command.ParameterParser;
import com.daanpanis.core.api.command.exceptions.CommandExecutionException;

public class FloatParser implements ParameterParser<Float> {

    @Override
    public Float parse(CommandArgument argument, String value) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException ex) {
            throw new CommandExecutionException(argument.getName() + " must be a decimal number!");
        }
    }
}
