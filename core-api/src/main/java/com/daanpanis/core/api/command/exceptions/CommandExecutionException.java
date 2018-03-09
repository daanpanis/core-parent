package com.daanpanis.core.api.command.exceptions;

public class CommandExecutionException extends CommandRuntimeException {

    private final String message;

    public CommandExecutionException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
