package com.daanpanis.core.api;

import com.daanpanis.core.api.command.CommandManager;
import com.daanpanis.filewatcher.FileWatchers;
import com.daanpanis.injection.DependencyInjector;
import org.bukkit.plugin.java.JavaPlugin;

public interface CoreApi {

    CommandManager getCommandManager();

    DependencyInjector getInjector();

    FileWatchers getFileWatchers();

    JavaPlugin getPlugin();

}
