package com.daanpanis.core.command;

import com.daanpanis.core.api.command.CommandArgument;

public class MessageCommandArgument implements CommandArgument {

    private final String name;
    private final int parameterIndex;

    public MessageCommandArgument(String name, int parameterIndex) {
        this.name = name;
        this.parameterIndex = parameterIndex;
    }

    public int getParameterIndex() {
        return parameterIndex;
    }

    @Override
    public String getName() {
        return name;
    }
}
