package com.daanpanis.core;

import com.daanpanis.core.api.Core;
import com.daanpanis.core.api.CoreApi;
import com.daanpanis.core.api.command.CommandManager;
import com.daanpanis.core.api.command.parsers.*;
import com.daanpanis.core.command.CommandScriptHandler;
import com.daanpanis.filewatcher.FileTracker;
import com.daanpanis.filewatcher.FileWatchers;
import com.daanpanis.filewatcher.github.GithubCredentialsParser;
import com.daanpanis.filewatcher.github.GithubTracker;
import com.daanpanis.filewatcher.local.LocalTracker;
import com.daanpanis.injection.DependencyInjector;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CorePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        Core.setApi(new CoreApiImpl());
        registerServices();
        registerCommandDefaults();
        //        setupFileWatchers();
    }

    public static void registerCommandDefaults() {
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

    public static void registerServices() {
        DependencyInjector injector = Core.getApi().getInjector();

        injector.addScoped(CoreApi.class, Core::getApi);

        injector.addSingleton(DependencyInjector.class, () -> injector);
        injector.addScoped(CommandManager.class, () -> Core.getApi().getCommandManager());
    }

    public static void setupFileWatchers() {
        FileWatchers watchers = Core.getApi().getFileWatchers();

        watchers.registerFileTracker(start(new LocalTracker()));
        watchers.registerFileTracker(start(new GithubTracker()));
        watchers.registerCredentialsParser(new GithubCredentialsParser());
        watchers.registerUpdateHandler(new CommandScriptHandler());
    }

    private static <T extends FileTracker> T start(T tracker) {
        tracker.startAsync();
        return tracker;
    }

}
