package com.daanpanis.core.command;

import com.daanpanis.core.api.command.CommandArgument;
import com.daanpanis.core.api.command.ParameterParser;

public class ParsableCommandArgument implements CommandArgument {

    private final String name;
    private final ParameterParser parser;

    public ParsableCommandArgument(String name, ParameterParser parser) {
        this.name = name;
        this.parser = parser;
    }

    public ParameterParser getParser() {
        return parser;
    }

    @Override
    public String getName() {
        return name;
    }
}