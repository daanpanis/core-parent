package com.daanpanis.core;

import com.daanpanis.core.api.Core;
import com.daanpanis.core.api.CoreApi;
import com.daanpanis.core.api.ban.BanService;
import com.daanpanis.core.api.command.CommandManager;
import com.daanpanis.core.api.command.parsers.*;
import com.daanpanis.core.ban.MySQLBanService;
import com.daanpanis.core.command.CommandScriptHandler;
import com.daanpanis.core.listener.ListenerScriptHandler;
import com.daanpanis.core.program.Debugger;
import com.daanpanis.database.mysql.MySQL;
import com.daanpanis.database.mysql.MySQLConfiguration;
import com.daanpanis.filewatcher.FileTracker;
import com.daanpanis.filewatcher.FileWatchers;
import com.daanpanis.filewatcher.exceptions.ConfigurationLoadException;
import com.daanpanis.filewatcher.github.GithubCredentialsParser;
import com.daanpanis.filewatcher.github.GithubTracker;
import com.daanpanis.filewatcher.local.LocalTracker;
import com.daanpanis.injection.DependencyInjector;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;

public class CorePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        Debugger.debug = false;
        saveDefaultConfig();
        saveResource("watchers.json", false);

        Core.setApi(new CoreApiImpl(this));
        registerServices();
        registerCommandDefaults();
        setupFileWatchers();
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
        manager.registerParameterType(GameMode.class, new GameModeParser());
    }

    public void registerServices() {
        DependencyInjector injector = Core.getApi().getInjector();

        injector.addScoped(CoreApi.class, Core::getApi);

        injector.addSingleton(DependencyInjector.class, () -> injector);
        injector.addScoped(CommandManager.class, () -> Core.getApi().getCommandManager());
        setupMySQL(injector);
        injector.addScoped(BanService.class, MySQLBanService.class);
    }

    public void setupFileWatchers() {
        FileWatchers watchers = Core.getApi().getFileWatchers();
        DependencyInjector injector = Core.getApi().getInjector();

        watchers.registerFileTracker(start(new LocalTracker()));
        watchers.registerFileTracker(start(new GithubTracker()));
        watchers.registerCredentialsParser(new GithubCredentialsParser());
        watchers.registerUpdateHandler(injector.inject(CommandScriptHandler.class));
        watchers.registerUpdateHandler(injector.inject(ListenerScriptHandler.class));

        try {
            System.out.println("Loading configuration");
            watchers.loadConfiguration(new File(getDataFolder(), "watchers.json"));
        } catch (FileNotFoundException | ConfigurationLoadException e) {
            e.printStackTrace();
        }
        System.out.println(watchers.getRegisteredTracker("local").getRules().size());
    }

    private static <T extends FileTracker> T start(T tracker) {
        tracker.startAsync();
        return tracker;
    }

    private void setupMySQL(DependencyInjector injector) {
        MySQL mysql = new MySQL(getMySQLConfiguration());
        mysql.connect();
        injector.addSingleton(MySQL.class, () -> mysql);
    }


    private MySQLConfiguration getMySQLConfiguration() {
        FileConfiguration config = getConfig();
        return new MySQLConfiguration(config.getString("mysql.host", "localhost"), config.getInt("mysql.port", 3306),
                config.getString("mysql.username", "root"), config.getString("mysql.password", "password"),
                config.getString("mysql.database", "database"));
    }

}
