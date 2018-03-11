package com.daanpanis.core;

import com.daanpanis.core.api.Core;
import com.daanpanis.core.api.command.CommandManager;
import com.daanpanis.core.api.command.parsers.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CorePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        Core.setApi(new CoreApiImpl());
        registerCommandDefaults();
    }

    private void registerCommandDefaults() {
        CommandManager manager = Core.getApi().getCommandManager();
        manager.registerParameterType(String.class, new StringParser());
        manager.registerParameterType(Integer.class, new IntegerParser());
        manager.registerParameterType(int.class, new IntegerParser());
        manager.registerParameterType(Long.class, new LongParser());
        manager.registerParameterType(long.class, new LongParser());
        manager.registerParameterType(Double.class, new DoubleParser());
        manager.registerParameterType(double.class, new DoubleParser());
        manager.registerParameterType(Float.class, new FloatParser());
        manager.registerParameterType(float.class, new FloatParser());
        manager.registerParameterType(Player.class, new PlayerParser());
    }

}
