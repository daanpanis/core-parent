package com.daanpanis.core.api.command.parsers;

import com.daanpanis.core.api.command.CommandArgument;
import com.daanpanis.core.api.command.ParameterParser;
import com.daanpanis.core.api.command.exceptions.CommandExecutionException;
import org.bukkit.GameMode;

public class GameModeParser implements ParameterParser<GameMode> {

    @Override
    public GameMode parse(CommandArgument argument, String value) {
        GameMode gameMode = getGameMode(value);

        if (gameMode == null)
            throw new CommandExecutionException("Unknown gamemode '" + value + "'");

        return gameMode;
    }

    private GameMode getGameMode(String value) {
        switch (value.toLowerCase()) {
            case "0":
            case "1":
            case "2":
            case "3":
                return GameMode.getByValue(Integer.parseInt(value));
            default:
                for (GameMode mode : GameMode.values()) {
                    if (mode.name().replace("_", "").equalsIgnoreCase(value.replace("_", "")))
                        return mode;
                }
                return null;
        }

    }
}
