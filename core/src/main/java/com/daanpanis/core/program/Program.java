package com.daanpanis.core.program;

import com.daanpanis.core.api.command.Command;
import com.daanpanis.core.api.command.CommandManager;
import com.daanpanis.core.api.command.parsers.DoubleParser;
import com.daanpanis.core.api.command.parsers.IntegerParser;
import com.daanpanis.core.api.command.parsers.StringParser;
import com.daanpanis.core.api.command.permission.DefaultPermissionHandler;
import com.daanpanis.core.api.command.permission.Permission;
import com.daanpanis.core.command.CoreCommandManager;
import org.bukkit.command.CommandSender;

import java.util.Scanner;

public class Program {

    static final CommandSender TEST_SENDER = new TestCommandSender();

    public static void main(String[] args) throws Exception {
        Debugger.debug = false;
        CommandManager manager = new CoreCommandManager();

        manager.registerPermissionHandler(Permission.class, new DefaultPermissionHandler());

        manager.registerParameterType(String.class, new StringParser());
        manager.registerParameterType(int.class, new IntegerParser());
        manager.registerParameterType(Integer.class, new IntegerParser());
        manager.registerParameterType(double.class, new DoubleParser());

        manager.registerCommands(new Object() {

            @Command(syntax = "command {1}")
            void command(CommandSender sender, double number) {
                sender.sendMessage(number + "");
            }

        });

        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            String line;
            while ((line = scanner.nextLine()) != null) {
                manager.executeCommand(TEST_SENDER, line);
            }
        }).start();
    }
}
