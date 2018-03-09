package com.daanpanis.core.command;

import com.daanpanis.core.api.command.CommandArgument;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;
import java.util.List;

public class CoreCommand {

    private final Object instance;
    private final Method method;
    private final List<CommandArgument> arguments;

    public CoreCommand(Object instance, Method method, List<CommandArgument> arguments) {
        this.instance = instance;
        this.method = method;
        this.arguments = arguments;
    }

    public boolean matches(List<String> args) {
        if (args.size() >= arguments.size()) {
            for (int i = 0; i < arguments.size(); i++) {
                CommandArgument argument = arguments.get(i);
                String value = args.get(i);

                if (argument instanceof StaticCommandArgument && !((StaticCommandArgument) argument).matches(value))
                    return false;
                if (i == arguments.size() - 1 && !(argument instanceof MessageCommandArgument) && args.size() > arguments.size())
                    return false;
            }
            return true;
        }
        return false;
    }

    public void execute(CommandSender sender, List<String> args) {
        Object[] parameters = new Object[method.getParameterTypes().length];
        parameters[0] = sender;
    }
}