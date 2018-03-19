package com.daanpanis.core.api.command;

import com.daanpanis.core.api.command.exceptions.CommandException;
import com.daanpanis.core.api.command.meta.Meta;
import com.daanpanis.core.api.command.meta.MetaMatcher;
import org.bukkit.command.CommandSender;

import java.lang.annotation.Annotation;
import java.util.List;

public interface CommandManager {

    default void registerCommands(Object commands) throws CommandException {
        registerCommands(commands, Meta.empty());
    }

    void registerCommands(Object commands, Meta meta) throws CommandException;

    void unregisterCommands(MetaMatcher matcher);

    <T> void registerParameterType(Class<T> parameterClass, ParameterParser<T> parser);

    boolean isParameterRegistered(Class<?> parameterClass);

    <T> ParameterParser<T> getParameterParser(Class<T> parameterClass);

    void executeCommand(CommandSender sender, String executedCommand);

    void executeCommand(CommandSender sender, String command, List<String> args);

    <T extends Annotation> void registerPermissionHandler(Class<T> annotationClass, PermissionHandler<T> handler);

    boolean isPermissionHandlerRegistered(Class<? extends Annotation> annotationClass);

}