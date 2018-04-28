package com.daanpanis.core.command.parsers;

import com.daanpanis.core.api.command.CommandArgument;
import com.daanpanis.core.api.command.ParameterParser;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class OfflinePlayerParser implements ParameterParser<OfflinePlayer> {

    @Override
    public OfflinePlayer parse(CommandArgument argument, String value) {
        return Bukkit.getOfflinePlayer(value);
    }
}
