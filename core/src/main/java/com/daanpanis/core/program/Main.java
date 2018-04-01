package com.daanpanis.core.program;

import com.daanpanis.core.CoreApiImpl;
import com.daanpanis.core.api.Core;
import com.daanpanis.core.api.command.exceptions.CommandExecutionException;
import com.daanpanis.core.command.CommandScriptHandler;
import com.daanpanis.core.command.defaults.HelpCommands;
import com.daanpanis.filewatcher.FileWatchers;
import com.daanpanis.filewatcher.github.GithubTracker;
import com.daanpanis.filewatcher.local.LocalTracker;
import org.bukkit.command.CommandSender;

import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import static com.daanpanis.core.CorePlugin.registerCommandDefaults;
//import static com.daanpanis.core.CorePlugin.registerServices;

public class Main {

    private static final CommandSender sender = new TestCommandSender();

    public static void main(String[] args) throws Exception {
        Debugger.debug = false;
        Core.setApi(new CoreApiImpl(null));
        //        registerServices();
        registerCommandDefaults();

        Core.getApi().getCommandManager().registerCommands(Core.getApi().getInjector().inject(HelpCommands.class));

        Core.getApi().getInjector().addScoped(FileWatchers.class, () -> Core.getApi().getFileWatchers());

        Core.getApi().getFileWatchers().registerFileTracker(new LocalTracker());
        Core.getApi().getFileWatchers().registerFileTracker(new GithubTracker());
        Core.getApi().getFileWatchers().registerUpdateHandler(Core.getApi().getInjector().inject(CommandScriptHandler.class));

        Core.getApi().getFileWatchers().getRegisteredTracker("local").startAsync();
        Core.getApi().getFileWatchers().getRegisteredTracker("github").startAsync();

        Core.getApi().getFileWatchers().loadConfiguration(Main.class.getResourceAsStream("/watchers.json"));

        CompletableFuture.runAsync(() -> {
            Scanner scanner = new Scanner(System.in);
            try {
                String line;
                while ((line = scanner.nextLine()) != null) {
                    Core.getApi().getCommandManager().executeCommand(sender, line);
                }
            } catch (CommandExecutionException ex) {
                System.err.println(ex.getMessage());
            }
        }, Executors.newSingleThreadExecutor());
    }

}
