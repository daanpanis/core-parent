package com.daanpanis.core.api;

import com.daanpanis.core.api.command.CommandManager;
import com.daanpanis.filewatcher.FileWatchers;
import com.daanpanis.injection.DependencyInjector;

public interface CoreApi {

    CommandManager getCommandManager();

    DependencyInjector getInjector();

    FileWatchers getFileWatchers();

}
