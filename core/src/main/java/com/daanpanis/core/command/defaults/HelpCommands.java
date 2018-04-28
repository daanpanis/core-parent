package com.daanpanis.core.command.defaults;

import com.daanpanis.core.api.command.Command;
import com.daanpanis.core.api.command.CommandManager;
import com.daanpanis.core.command.CoreCommandManager;
import com.daanpanis.core.command.MessageCommandArgument;
import com.daanpanis.core.command.ParsableCommandArgument;
import com.daanpanis.core.command.StaticCommandArgument;
import com.daanpanis.injection.Inject;
import org.bukkit.command.CommandSender;

public class HelpCommands {

    @Inject(castFrom = CommandManager.class)
    CoreCommandManager manager;

    @Command(syntax = "help2")
    void help(CommandSender sender) {
        manager.getRegisteredCommands().forEach((command, coreCommands) -> {
            coreCommands.forEach(coreCommand -> sender.sendMessage(coreCommand.getSyntax(command)));
        });
    }
}
