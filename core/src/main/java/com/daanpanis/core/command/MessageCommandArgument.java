package com.daanpanis.core.command;

import com.daanpanis.core.api.command.CommandArgument;

public class MessageCommandArgument implements CommandArgument {

    private final String name;

    public MessageCommandArgument(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
