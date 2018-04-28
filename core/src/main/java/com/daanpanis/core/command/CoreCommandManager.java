package com.daanpanis.core.command;

import com.daanpanis.core.api.command.*;
import com.daanpanis.core.api.command.exceptions.*;
import com.daanpanis.core.api.command.meta.Meta;
import com.daanpanis.core.api.command.meta.MetaMatcher;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CoreCommandManager implements CommandManager {

    private static CommandMap commandMap;

    static {
        if (Bukkit.getServer() != null) {
            SimplePluginManager pluginManager = (SimplePluginManager) Bukkit.getServer().getPluginManager();
            if (pluginManager != null) {
                try {
                    Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                    field.setAccessible(true);
                    commandMap = (CommandMap) field.get(pluginManager);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private final Map<String, Collection<CoreCommand>> registeredCommands = new HashMap<>();
    private final Map<String, BukkitCommand> bukkitCommands = new HashMap<>();
    private final Map<Class<?>, ParameterParser<?>> registeredParameters = new HashMap<>();
    private final Map<Class<? extends Annotation>, PermissionHandler<?>> permissionHandlers = new HashMap<>();

    public Map<String, Collection<CoreCommand>> getRegisteredCommands() {
        return registeredCommands;
    }

    @Override
    public void registerCommands(Object commands, Meta meta) throws CommandException {
        Collection<Method> commandMethods = Stream.of(commands.getClass().getDeclaredMethods())
                .filter(method -> method.getAnnotation(Command.class) != null).collect(Collectors.toList());
        if (commandMethods.isEmpty())
            throw new CommandException("No command methods found");
        for (Method method : commandMethods) {
            registerCommand(method, commands, meta);
        }
    }

    @Override
    public void unregisterCommands(MetaMatcher matcher) {
        registeredCommands.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(coreCommand -> matcher.test(coreCommand.getMeta()));
            return entry.getValue().isEmpty();
        });
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
        this.executeCommand(sender, commandSplit.get(0).toLowerCase(), commandSplit.subList(1, commandSplit.size()));
    }

    @Override
    public void executeCommand(CommandSender sender, String command, List<String> args) {
        if (registeredCommands.containsKey(command)) {
            List<CommandScore> commands = getMostLikely(registeredCommands.get(command), args);

            if (commands.isEmpty()) {
                throw new CommandExecutionException("Unknown command!");
            }

            CommandScore best = commands.get(0);
            if (best.command.matches(args)) {
                best.command.execute(sender, args);
            } else {
                if (commands.stream().filter(commandScore -> commandScore.score == best.score).count() == 1)
                    sender.sendMessage(ChatColor.RED.toString() + "Correct usage: " + best.command.getSyntax(command));
                else
                    throw new CommandExecutionException("Multiple matches found!");
            }
        } else {
            throw new CommandExecutionException("Unknown command!");
        }
    }

    private List<CommandScore> getMostLikely(Collection<CoreCommand> allCommands, List<String> args) {
        return allCommands.stream().map(command -> new CommandScore(command, command.matchRating(args)))
                .sorted((o1, o2) -> Integer.compare(o2.score, o1.score)).collect(Collectors.toList());
    }

    private class CommandScore {

        private final CoreCommand command;
        private final int score;

        public CommandScore(CoreCommand command, int score) {
            this.command = command;
            this.score = score;
        }
    }

    @Override
    public <T extends Annotation> void registerPermissionHandler(Class<T> annotationClass, PermissionHandler<T> handler) {
        Preconditions.checkNotNull(annotationClass, "The annotation class can't be null");
        Preconditions.checkNotNull(handler, "The permisison handler can't be null");
        permissionHandlers.put(annotationClass, handler);
    }

    @Override
    public boolean isPermissionHandlerRegistered(Class<? extends Annotation> annotationClass) {
        return permissionHandlers.containsKey(annotationClass);
    }

    private BukkitCommand registerBukkitCommand(String commandName) {
        BukkitCommand command = new BukkitCommand(commandName, this);
        if (commandMap != null) {
            commandMap.register(commandName, command);
            if (Bukkit.getServer().getPluginCommand(commandName) != null) {
                Bukkit.getServer().getPluginCommand(commandName).setExecutor(command);
            }
        }
        return command;
    }

    private void registerCommand(Method method, Object instance, Meta meta) throws CommandException {
        List<Parameter> parameters = getParameters(method);

        Command annotation = method.getAnnotation(Command.class);
        List<String> arguments = Stream.of(annotation.syntax().trim().replace("\\s{2,}", "").split(" ")).filter(string -> !string.isEmpty())
                .collect(Collectors.toList());

        if (arguments.isEmpty())
            throw new CommandSyntaxEmptyException("The syntax is empty!");

        String commandName = arguments.get(0);
        arguments = arguments.subList(1, arguments.size());
        List<CommandArgument> args = getArguments(method, parameters, arguments);

        Annotation permissionAnnotation = getPermissionAnnotation(method);

        this.registeredCommands.computeIfAbsent(commandName.toLowerCase(), f -> new ArrayList<>())
                .add(new CoreCommand(instance, method, args, meta, permissionAnnotation,
                        permissionAnnotation != null ? permissionHandlers.get(permissionAnnotation.annotationType()) : null));
        this.bukkitCommands.put(commandName.toLowerCase(), new BukkitCommand(commandName, this));
        registerBukkitCommand(commandName);
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
            if (!isParameterRegistered(parameter.getType())) {
                throw new UnregisteredParameterException("Unregistered parameter " + parameter + ", index: " + i);
            }
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
        if (index >= parameters.size() + 1) {
            throw new CommandSyntaxException(
                    "Argument index number bigger than parameter count (given: " + index + ", parameter count: " + parameters.parallelStream() + ")");
        }
        if (indexesUsed.contains(index))
            throw new CommandSyntaxException("Can't use one argument index multiple times");
        Parameter parameter = parameters.get(index - 1);
        String argumentName = getArgumentName(parameter);
        if (parameter.getAnnotation(Message.class) != null) {
            if (parameter.getType() != String.class) {
                throw new CommandParametersException("Message annotation can only be applied to a String parameter");
            }

            return new MessageCommandArgument(argumentName, index);
        }

        indexesUsed.add(index);
        return new ParsableCommandArgument(argumentName, index, getParameterParser(parameter.getType()));
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

    private Annotation getPermissionAnnotation(Method method) {
        for (Annotation annotation : method.getDeclaredAnnotations()) {
            if (permissionHandlers.containsKey(annotation.annotationType())) {
                return annotation;
            }
        }
        return null;
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
