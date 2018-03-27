package com.daanpanis.core.command;

import com.daanpanis.core.api.command.CommandManager;
import com.daanpanis.core.api.command.exceptions.CommandExecutionException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class BukkitCommand extends Command implements CommandExecutor {

    private final CommandManager commandManager;

    protected BukkitCommand(String name, CommandManager commandManager) {
        super(name);
        this.commandManager = commandManager;
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        try {
            commandManager.executeCommand(commandSender, s, Arrays.asList(strings));

            return true;
        } catch (CommandExecutionException ex) {
            commandSender.sendMessage(ex.getMessage());
        }
        return false;
    }

    public BukkitCommand addAlias(String alias) {
        return addAliases(Collections.singletonList(alias));
    }

    public BukkitCommand addAliases(Collection<String> aliases) {
        getAliases().addAll(aliases);
        return this;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        return execute(commandSender, s, strings);
    }
}
