package com.daanpanis.core.program;

import com.daanpanis.core.api.command.Command;
import com.daanpanis.core.api.command.CommandManager;
import com.daanpanis.core.api.command.Message;
import com.daanpanis.core.api.command.Name;
import com.daanpanis.core.api.command.parsers.IntegerParser;
import com.daanpanis.core.api.command.parsers.StringParser;
import com.daanpanis.core.command.CoreCommandManager;
import org.bukkit.command.CommandSender;

import java.util.Scanner;

public class Program {

    static final CommandSender TEST_SENDER = new TestCommandSender();

    public static void main(String[] args) throws Exception {
        Debugger.debug = false;
        CommandManager manager = new CoreCommandManager();

        manager.registerParameterType(String.class, new StringParser());
        manager.registerParameterType(int.class, new IntegerParser());
        manager.registerParameterType(Integer.class, new IntegerParser());

        manager.registerCommands(new Object() {

            @Command(syntax = "command [lel,test] {1} {2}")
            void command(CommandSender sender, @Name(name = "amount") int number, @Message String arg1) {
                sender.sendMessage(number + ": " + arg1);
            }

        });

        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            String line;
            while ((line = scanner.nextLine()) != null) {

            }
        }).start();

        manager.executeCommand(TEST_SENDER, "command test a kappa kappa2 kapap");
    }

}
