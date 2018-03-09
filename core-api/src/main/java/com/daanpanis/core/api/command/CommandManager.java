package com.daanpanis.core.api.command;

import com.daanpanis.core.api.command.exceptions.CommandException;
import org.bukkit.command.CommandSender;

public interface CommandManager {

    void registerCommands(Object commands) throws CommandException;

    <T> void registerParameterType(Class<T> parameterClass, ParameterParser<T> parser);

    boolean isParameterRegistered(Class<?> parameterClass);

    <T> ParameterParser<T> getParameterParser(Class<T> parameterClass);

    void executeCommand(CommandSender sender, String executedCommand);

}