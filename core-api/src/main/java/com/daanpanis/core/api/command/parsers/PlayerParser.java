package com.daanpanis.core.api.command.parsers;

import com.daanpanis.core.api.command.CommandArgument;
import com.daanpanis.core.api.command.ParameterParser;
import com.daanpanis.core.api.command.exceptions.CommandExecutionException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerParser implements ParameterParser<Player> {

    @Override
    public Player parse(CommandArgument argument, String value) {
        Player player = Bukkit.getPlayer(value);

        if (player == null) throw new CommandExecutionException(value + " isn't online!");

        return player;
    }
}
