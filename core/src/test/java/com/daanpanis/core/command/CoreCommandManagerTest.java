package com.daanpanis.core.command;

import com.daanpanis.core.api.command.Command;
import com.daanpanis.core.api.command.Message;
import com.daanpanis.core.api.command.exceptions.*;
import com.daanpanis.core.api.command.meta.Meta;
import com.daanpanis.core.api.command.parsers.IntegerParser;
import com.daanpanis.core.api.command.parsers.StringParser;
import com.daanpanis.core.api.command.permission.DefaultPermissionHandler;
import com.daanpanis.core.api.command.permission.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class CoreCommandManagerTest {

    private CoreCommandManager manager;

    @Before
    public void setup() {
        this.manager = new CoreCommandManager();
        this.manager.registerParameterType(String.class, new StringParser());
    }

    @Test
    public void registerCommandNoArguments() throws Exception {
        manager.registerCommands(new Object() {

            @Command(syntax = "test")
            public void command(CommandSender sender) {
            }

        });
        assertThat(manager.getCommandsCount(), is(1));
    }

    @Test(expected = CommandException.class)
    public void registerCommandsNoCommandMethods() throws Exception {
        manager.registerCommands(new Object());
    }

    @Test(expected = CommandParametersException.class)
    public void registerCommandNoParameters() throws Exception {
        manager.registerCommands(new Object() {

            @Command(syntax = "test")
            void command() {
            }

        });
    }

    @Test(expected = CommandParametersException.class)
    public void registerCommandFirstParameterNotSender() throws Exception {
        manager.registerCommands(new Object() {

            @Command(syntax = "test {1}")
            void command(String arg1) {
            }

        });
    }


    @Test(expected = UnregisteredParameterException.class)
    public void registerCommandUnregisteredParameter() throws Exception {
        manager.registerCommands(new Object() {

            @Command(syntax = "test {1}")
            void command(CommandSender sender, Player player) {
            }

        });
    }

    @Test(expected = CommandSyntaxEmptyException.class)
    public void registerCommandEmptySyntax() throws Exception {
        manager.registerCommands(new Object() {

            @Command(syntax = "")
            void command(CommandSender sender) {
            }

        });
    }

    @Test(expected = CommandSyntaxEmptyException.class)
    public void registerCommandEmptySyntaxWithSpaces() throws Exception {
        manager.registerCommands(new Object() {

            @Command(syntax = "   ")
            void command(CommandSender sender) {
            }

        });
    }

    @Test(expected = CommandSyntaxException.class)
    public void registerCommandIndexLowerThanOne() throws Exception {
        manager.registerCommands(new Object() {

            @Command(syntax = "command {0}")
            void command(CommandSender sender, String arg) {
            }

        });
    }

    @Test(expected = CommandSyntaxException.class)
    public void registerCommandIndexTooBig() throws Exception {
        manager.registerCommands(new Object() {

            @Command(syntax = "command {2}")
            void command(CommandSender sender, String arg) {
            }

        });
    }

    @Test(expected = CommandSyntaxException.class)
    public void registerCommandIndexNotNumber() throws Exception {
        manager.registerCommands(new Object() {

            @Command(syntax = "command {ab}")
            void command(CommandSender sender, String arg) {
            }

        });
    }

    @Test(expected = CommandSyntaxException.class)
    public void registerCommandIndexDuplicate() throws Exception {
        manager.registerCommands(new Object() {

            @Command(syntax = "command {1} {1}")
            void command(CommandSender sender, String arg1, String arg2) {
            }

        });
    }

    @Test(expected = CommandSyntaxException.class)
    public void registerCommandMessageParameterNotLastArgument() throws Exception {
        manager.registerCommands(new Object() {

            @Command(syntax = "command {2} {1}")
            void command(CommandSender sender, String arg1, @Message String message) {
            }

        });
    }

    @Test
    public void registerCommandParameterMessageArgument() throws Exception {
        manager.registerCommands(new Object() {

            @Command(syntax = "command {1} {2}")
            void command(CommandSender sender, String arg1, @Message String message) {
            }

        });
        assertThat(manager.getCommandsCount(), is(1));
    }

    @Test(expected = CommandParametersException.class)
    public void registerCommandMessageArgumentNotString() throws Exception {
        manager.registerParameterType(int.class, new IntegerParser());
        manager.registerCommands(new Object() {

            @Command(syntax = "command {1}")
            void command(CommandSender sender, @Message int message) {
            }

        });
    }

    /*
     * Unregister tests
     */
    @Test
    public void unregisterSingleCommand() throws Exception {
        manager.registerCommands(new Object() {

            @Command(syntax = "command")
            void command(CommandSender sender) {
            }

        }, Meta.builder().value("id", "test-command").build());
        assertThat(manager.getCommandsCount(), is(1));
        manager.unregisterCommands(meta -> meta.is("id", "test-command"));
        assertThat(manager.getCommandsCount(), is(0));
    }

    @Test
    public void unregisterMultipleCommands() throws Exception {
        manager.registerCommands(new Object() {

            @Command(syntax = "command1")
            void command1(CommandSender sender) {
            }

            @Command(syntax = "command2")
            void command2(CommandSender sender) {
            }

        }, Meta.builder().value("id", "test-command").build());
        assertThat(manager.getCommandsCount(), is(2));
        manager.unregisterCommands(meta -> meta.is("id", "test-command"));
        assertThat(manager.getCommandsCount(), is(0));
    }

    @Test
    public void unregisterMultipleSubCommands() throws Exception {
        manager.registerCommands(new Object() {

            @Command(syntax = "command")
            void command(CommandSender sender) {
            }

            @Command(syntax = "command sub")
            void subCommand(CommandSender sender) {
            }

        }, Meta.builder().value("id", "test-command").build());
        assertThat(manager.getCommandsCount(), is(2));
        manager.unregisterCommands(meta -> meta.is("id", "test-command"));
        assertThat(manager.getCommandsCount(), is(0));
    }

    /*
     * Parameter type tests
     */
    @Test
    public void registerParameter() {
        manager.registerParameterType(String.class, (argument, value) -> null);
        assertThat(manager.isParameterRegistered(String.class), is(true));
        assertThat(manager.getParameterParser(String.class), is(notNullValue()));
    }

    @Test(expected = NullPointerException.class)
    public void registerParameterTypeNull() {
        manager.registerParameterType(null, (argument, value) -> null);
    }

    @Test(expected = NullPointerException.class)
    public void registerParameterParserNull() {
        manager.registerParameterType(String.class, null);
    }

    @Test
    public void registerParameterDuplicate() {
        manager.registerParameterType(String.class, (argument, value) -> "1");
        manager.registerParameterType(String.class, (argument, value) -> "2");
        assertThat(manager.isParameterRegistered(String.class), is(true));
        assertThat(manager.getParameterParser(String.class), is(notNullValue()));
        assertThat(manager.getParameterParser(String.class).parse(null, null), is("2"));
    }

    /*
     * Permission handler tests
     */
    @Test
    public void registerPermissionHandler() {
        manager.registerPermissionHandler(Permission.class, new DefaultPermissionHandler());
        assertThat(manager.isPermissionHandlerRegistered(Permission.class), is(true));
    }
}