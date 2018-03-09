package com.daanpanis.core.api.command.parsers;

import com.daanpanis.core.api.command.CommandArgument;
import com.daanpanis.core.api.command.ParameterParser;
import com.daanpanis.core.api.command.exceptions.CommandExecutionException;

public class DoubleParser implements ParameterParser<Double> {

    @Override
    public Double parse(CommandArgument argument, String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            throw new CommandExecutionException(argument.getName() + " must be a decimal number!");
        }
    }
}
