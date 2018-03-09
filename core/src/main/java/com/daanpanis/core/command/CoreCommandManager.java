package com.daanpanis.core.command;

import com.daanpanis.core.api.command.*;
import com.daanpanis.core.api.command.exceptions.*;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CoreCommandManager implements CommandManager {

    private final Map<String, Collection<CoreCommand>> registeredCommands = new HashMap<>();
    private final Map<Class<?>, ParameterParser<?>> registeredParameters = new HashMap<>();

    @Override
    public void registerCommands(Object commands) throws CommandException {
        Collection<Method> commandMethods = Stream.of(commands.getClass().getDeclaredMethods())
                .filter(method -> method.getAnnotation(Command.class) != null).collect(Collectors.toList());
        if (commandMethods.isEmpty())
            throw new CommandException("No command methods found");
        for (Method method : commandMethods) {
            registerCommand(method, commands);
        }
    }

    @Override
    public <T> void registerParameterType(Class<T> parameterClass, ParameterParser<T> parser) {
        Preconditions.checkNotNull(parameterClass, "The parameter class can't be null");
        Preconditions.checkNotNull(parser, "The parser can't be null");
        this.registeredParameters.put(parameterClass, parser);
    }

    @Override
    public boolean isParameterRegistered(Class<?> parameterClass) {
        return this.registeredParameters.containsKey(parameterClass);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ParameterParser<T> getParameterParser(Class<T> parameterClass) {
        return (ParameterParser<T>) this.registeredParameters.get(parameterClass);
    }

    @Override
    public void executeCommand(CommandSender sender, String executedCommand) {
        List<String> commandSplit = Stream.of(executedCommand.trim().replace("\\s{2,}", "").split(" ")).filter(string -> !string.isEmpty())
                .collect(Collectors.toList());
        if (commandSplit.isEmpty())
            throw new CommandExecutionException("Unknown command!");
        String commandName = commandSplit.get(0).toLowerCase();
        if (registeredCommands.containsKey(commandName)) {
            List<String> args = commandSplit.subList(1, commandSplit.size() - 1);
            List<CoreCommand> commands = registeredCommands.get(commandName).stream().filter(coreCommand -> coreCommand.matches(args))
                    .collect(Collectors.toList());

            if (commands.isEmpty())
                throw new CommandExecutionException("Unknown command!");
            else if (commands.size() > 1)
                throw new CommandExecutionException("Multiple commands matched!");


        } else {
            throw new CommandExecutionException("Unknown command!");
        }
    }

    private void registerCommand(Method method, Object instance) throws CommandException {
        List<Parameter> parameters = getParameters(method);

        Command annotation = method.getAnnotation(Command.class);
        List<String> arguments = Stream.of(annotation.syntax().trim().replace("\\s{2,}", "").split(" ")).filter(string -> !string.isEmpty())
                .collect(Collectors.toList());

        if (arguments.isEmpty())
            throw new CommandSyntaxEmptyException("The syntax is empty!");

        String commandName = arguments.get(0);
        arguments = arguments.subList(1, arguments.size());
        List<CommandArgument> args = getArguments(method, parameters, arguments);

        this.registeredCommands.computeIfAbsent(commandName.toLowerCase(), f -> new ArrayList<>()).add(new CoreCommand(instance, method, args));
    }

    private List<Parameter> getParameters(Method method) throws CommandException {
        Parameter[] parameters = method.getParameters();
        if (parameters.length == 0)
            throw new CommandParametersException("Command method can't have zero parameters");

        Parameter first = parameters[0];
        if (first.getType() != CommandSender.class && first.getType() != Player.class && first.getType() != ConsoleCommandSender.class) {
            throw new CommandParametersException("First parameter needs to be the sender");
        }

        List<Parameter> params = new ArrayList<>();
        for (int i = 1; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (!isParameterRegistered(parameter.getType()))
                throw new UnregisteredParameterException("Unregistered parameter " + parameter + ", index: " + i);
            params.add(parameter);
        }
        return params;
    }

    private List<CommandArgument> getArguments(Method method, List<Parameter> parameters, List<String> arguments) throws CommandException {
        Set<Integer> indexesUsed = new HashSet<>();
        List<CommandArgument> result = new ArrayList<>();

        for (int i = 0; i < arguments.size(); i++) {
            String argument = arguments.get(i);
            if (argument.startsWith("{") && argument.endsWith("}")) {
                CommandArgument parsableArgument = parsableArgument(method, parameters, argument, indexesUsed);
                if (parsableArgument instanceof MessageCommandArgument && i < arguments.size() - 1) {
                    throw new CommandSyntaxException("A message argument must always be the last parameter");
                }

                result.add(parsableArgument);
            } else {
                result.add(staticArgument(argument));
            }
        }

        return result;
    }


    private CommandArgument parsableArgument(Method method, List<Parameter> parameters, String argument, Set<Integer> indexesUsed)
            throws CommandException {
        argument = argument.substring(1, argument.length() - 1);
        Integer index = parseInt(argument);
        if (index == null)
            throw new CommandSyntaxException("Wrong argument index given (" + argument + ")");
        if (index < 1)
            throw new CommandSyntaxException("Argument index must be 1 or higher");
        if (index >= parameters.size() + 1)
            throw new CommandSyntaxException(
                    "Argument index number bigger than parameter count (given: " + index + ", parameter count: " + parameters.parallelStream() + ")");
        if (indexesUsed.contains(index))
            throw new CommandSyntaxException("Can't use one argument index multiple times");
        Parameter parameter = parameters.get(index - 1);
        String argumentName = getArgumentName(parameter);
        if (parameter.getAnnotation(Message.class) != null) {
            return new MessageCommandArgument(argumentName);
        }

        indexesUsed.add(index);
        return new ParsableCommandArgument(argumentName, getParameterParser(parameter.getClass()));
    }

    private String getArgumentName(Parameter parameter) {
        Name name = parameter.getAnnotation(Name.class);
        if (name != null)
            return name.name();
        return parameter.getName();
    }

    private CommandArgument staticArgument(String argument) {
        if (argument.startsWith("[") && argument.endsWith("]")) {
            String[] aliases = argument.substring(1, argument.length() - 1).split(",");
            return new StaticCommandArgument(null, Sets.newHashSet(aliases));
        }
        return new StaticCommandArgument(null, Sets.newHashSet(argument));
    }

    private Integer parseInt(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public int getCommandsCount() {
        return this.registeredCommands.values().stream().mapToInt(Collection::size).sum();
    }
}