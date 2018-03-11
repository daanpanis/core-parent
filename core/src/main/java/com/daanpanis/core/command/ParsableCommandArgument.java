package com.daanpanis.core.command;

import com.daanpanis.core.api.command.CommandArgument;
import com.daanpanis.core.api.command.ParameterParser;

public class ParsableCommandArgument implements CommandArgument {

    private final String name;
    private final int parameterIndex;
    private final ParameterParser parser;

    public ParsableCommandArgument(String name, int parameterIndex, ParameterParser parser) {
        this.name = name;
        this.parameterIndex = parameterIndex;
        this.parser = parser;
    }

    public ParameterParser getParser() {
        return parser;
    }

    public int getParameterIndex() {
        return parameterIndex;
    }

    @Override
    public String getName() {
        return name;
    }
}