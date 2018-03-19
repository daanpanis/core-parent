package com.daanpanis.core;

import com.daanpanis.core.api.CoreApi;
import com.daanpanis.core.api.command.CommandManager;
import com.daanpanis.core.command.CoreCommandManager;
import com.daanpanis.injection.DependencyInjector;
import com.daanpanis.injection.impl.ServiceInjector;

public class CoreApiImpl implements CoreApi {

    private final CommandManager commandManager = new CoreCommandManager();
    private final DependencyInjector dependencyInjector = new ServiceInjector();

    @Override
    public CommandManager getCommandManager() {
        return commandManager;
    }

    @Override
    public DependencyInjector getInjector() {
        return dependencyInjector;
    }
}
