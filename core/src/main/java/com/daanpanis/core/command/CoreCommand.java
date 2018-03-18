package com.daanpanis.core.command;

import com.daanpanis.core.api.command.CommandArgument;
import com.daanpanis.core.api.command.PermissionHandler;
import com.daanpanis.core.api.command.exceptions.CommandExecutionException;
import com.daanpanis.core.api.command.meta.Meta;
import com.daanpanis.core.program.Debugger;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class CoreCommand {

    private final Object instance;
    private final Method method;
    private final List<CommandArgument> arguments;
    private final Meta meta;
    private final Annotation permissionAnnotation;
    private final PermissionHandler permissionHandler;

    public CoreCommand(Object instance, Method method, List<CommandArgument> arguments, Meta meta, Annotation permissionAnnotation,
            PermissionHandler<?> permissionHandler) {
        this.instance = instance;
        this.method = method;
        this.meta = meta;
        this.permissionAnnotation = permissionAnnotation;
        this.permissionHandler = permissionHandler;
        this.method.setAccessible(true);
        this.arguments = arguments;
    }

    public boolean matches(List<String> args) {
        if (args.size() >= arguments.size()) {
            for (int i = 0; i < arguments.size(); i++) {
                CommandArgument argument = arguments.get(i);
                String value = args.get(i);

                if (argument instanceof StaticCommandArgument && !((StaticCommandArgument) argument).matches(value)) return false;
                if (i == arguments.size() - 1 && !(argument instanceof MessageCommandArgument) && args.size() > arguments.size()) return false;
            }
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public void execute(CommandSender sender, List<String> args) {
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] parameters = new Object[paramTypes.length];
        parameters[0] = sender;

        if (paramTypes[0].equals(Player.class) && !(sender instanceof Player)) {
            throw new CommandExecutionException("You must be an in-game player to execute this command!");
        }
        if (paramTypes[0].equals(ConsoleCommandSender.class) && !(sender instanceof ConsoleCommandSender)) {
            throw new CommandExecutionException("Only the console can execute this command!");
        }

        System.out.println(permissionAnnotation);
        System.out.println(permissionHandler);
        if (permissionAnnotation != null && permissionHandler != null && !permissionHandler.hasPermission(sender, permissionAnnotation)) {
            throw new CommandExecutionException("Insufficient permissions!");
        }

        for (int i = 0; i < arguments.size(); i++) {
            CommandArgument argument = arguments.get(i);
            if (argument instanceof ParsableCommandArgument) {
                ParsableCommandArgument parsable = (ParsableCommandArgument) argument;
                Debugger.println("Parsable: " + parsable);
                Debugger.println("Arg: " + args.get(i));
                Debugger.println("Parser: " + parsable.getParser());
                parameters[parsable.getParameterIndex()] = parsable.getParser().parse(parsable, args.get(i));
            } else if (argument instanceof MessageCommandArgument) {
                MessageCommandArgument message = (MessageCommandArgument) argument;
                parameters[message.getParameterIndex()] = getMessage(args, i);
                break;
            }
        }

        try {
            this.method.invoke(this.instance, parameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private String getMessage(List<String> args, int startIndex) {
        StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i < args.size(); i++) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(args.get(i));
        }
        return sb.toString();
    }

    public Meta getMeta() {
        return meta;
    }
}