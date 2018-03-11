package com.daanpanis.core;

import com.daanpanis.core.api.CoreApi;
import com.daanpanis.core.api.command.CommandManager;
import com.daanpanis.core.command.CoreCommandManager;

public class CoreApiImpl implements CoreApi {

    private final CommandManager commandManager = new CoreCommandManager();

    @Override
    public CommandManager getCommandManager() {
        return commandManager;
    }
}
