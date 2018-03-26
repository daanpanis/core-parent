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

    @Command(syntax = "help")
    void help(CommandSender sender) {
        manager.getRegisteredCommands().forEach((command, coreCommands) -> {
            coreCommands.forEach(coreCommand -> {
                StringBuilder msg = new StringBuilder();
                msg.append("/").append(command).append(" ");
                coreCommand.getArguments().forEach(argument -> {
                    if (argument instanceof StaticCommandArgument) {
                        msg.append(getStaticNotation((StaticCommandArgument) argument));
                    } else if (argument instanceof ParsableCommandArgument) {
                        msg.append(getParsableNotation((ParsableCommandArgument) argument));
                    } else if (argument instanceof MessageCommandArgument) {
                        msg.append(getMessageNotation((MessageCommandArgument) argument));
                    }
                    msg.append(" ");
                });
                sender.sendMessage(msg.toString());
            });
        });
    }

    private String getStaticNotation(StaticCommandArgument argument) {
        StringBuilder sb = new StringBuilder();
        if (argument.getValues().size() == 1) {
            return argument.getValues().stream().findFirst().orElse("");
        }
        argument.getValues().forEach(s -> {
            if (sb.length() > 0) sb.append(" / ");
            sb.append(s);
        });
        return "[" + sb.toString() + "]";
    }

    private String getParsableNotation(ParsableCommandArgument argument) {
        return "<" + argument.getName() + ">";
    }

    private String getMessageNotation(MessageCommandArgument argument) {
        return "{" + argument.getName() + "}";
    }

}
