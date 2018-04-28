package com.daanpanis.core.command;

import com.daanpanis.core.api.command.CommandArgument;
import com.daanpanis.core.api.command.PermissionHandler;
import com.daanpanis.core.api.command.exceptions.CommandExecutionException;
import com.daanpanis.core.api.command.meta.Meta;
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
       /* if (args.size() >= arguments.size()) {
            for (int i = 0; i < arguments.size(); i++) {
                CommandArgument argument = arguments.get(i);
                String value = args.get(i);

                if (argument instanceof StaticCommandArgument && !((StaticCommandArgument) argument).matches(value))
                    return false;
                if (i == arguments.size() - 1 && !(argument instanceof MessageCommandArgument) && args.size() > arguments.size())
                    return false;
            }
            return true;
        }*/
        int score = matchRating(args);
        return score >= arguments.size() && score == args.size();
    }

    @SuppressWarnings("all")
    public int matchRating(List<String> args) {
        int score = 0;
        if (args.size() >= arguments.size()) {
            for (int i = 0; i < arguments.size(); i++) {
                CommandArgument argument = arguments.get(i);
                String value = args.get(i);

                if (argument instanceof StaticCommandArgument && !((StaticCommandArgument) argument).matches(value))
                    break;
                if (i == arguments.size() - 1 && !(argument instanceof MessageCommandArgument) && args.size() > arguments.size())
                    break;

                if (argument instanceof MessageCommandArgument)
                    score += args.size() - i;
                else
                    score++;
            }
        }
        return score;
    }

    public List<CommandArgument> getArguments() {
        return arguments;
    }

    public String getSyntax(String commandLabel) {
        StringBuilder msg = new StringBuilder();
        msg.append(commandLabel).append(" ");
        this.getArguments().forEach(argument -> {
            if (argument instanceof StaticCommandArgument) {
                msg.append(getStaticNotation((StaticCommandArgument) argument));
            } else if (argument instanceof ParsableCommandArgument) {
                msg.append(getParsableNotation((ParsableCommandArgument) argument));
            } else if (argument instanceof MessageCommandArgument) {
                msg.append(getMessageNotation((MessageCommandArgument) argument));
            }
            msg.append(" ");
        });
        return msg.toString();
    }

    private String getStaticNotation(StaticCommandArgument argument) {
        StringBuilder sb = new StringBuilder();
        if (argument.getValues().size() == 1) {
            return argument.getValues().stream().findFirst().orElse("");
        }
        argument.getValues().forEach(s -> {
            if (sb.length() > 0)
                sb.append(" / ");
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

        if (permissionAnnotation != null && permissionHandler != null && !permissionHandler.hasPermission(sender, permissionAnnotation)) {
            throw new CommandExecutionException("Insufficient permissions!");
        }

        for (int i = 0; i < arguments.size(); i++) {
            CommandArgument argument = arguments.get(i);
            if (argument instanceof ParsableCommandArgument) {
                ParsableCommandArgument parsable = (ParsableCommandArgument) argument;
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
            if (sb.length() > 0)
                sb.append(" ");
            sb.append(args.get(i));
        }
        return sb.toString();
    }

    public Meta getMeta() {
        return meta;
    }
}