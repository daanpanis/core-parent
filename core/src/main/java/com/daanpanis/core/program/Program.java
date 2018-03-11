package com.daanpanis.core.program;

import com.daan.scripting.loading.impl.ScriptLoaderImpl;
import com.daanpanis.core.api.command.Command;
import com.daanpanis.core.api.command.CommandManager;
import com.daanpanis.core.api.command.Message;
import com.daanpanis.core.api.command.Name;
import com.daanpanis.core.api.command.exceptions.CommandException;
import com.daanpanis.core.api.command.meta.Meta;
import com.daanpanis.core.api.command.parsers.IntegerParser;
import com.daanpanis.core.api.command.parsers.StringParser;
import com.daanpanis.core.command.CoreCommandManager;
import com.daanpanis.core.watcher.FolderWatcher;
import com.daanpanis.core.watcher.UpdateHandler;
import com.daanpanis.scripting.loading.api.ScriptLoader;
import com.daanpanis.scripting.loading.api.exception.ScriptException;
import com.daanpanis.scripting.loading.groovy.GroovyCompiler;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
                manager.executeCommand(TEST_SENDER, line);
            }
        }).start();

        new FolderWatcher().setFilter(file -> file.getName().toLowerCase().endsWith(".groovy")).addFolder("C:/Users/Daan Panis/Desktop/Commands")
                .setHandler(new UpdateHandler() {

                    ExecutorService service = Executors.newCachedThreadPool();
                    ScriptLoader loader = new ScriptLoaderImpl(new GroovyCompiler());

                    @Override
                    public void onAdded(List<File> files) {
                        for (Map.Entry<File, Object> entry : loadScripts(files).entrySet()) {
                            try {
                                manager.registerCommands(entry.getValue(),
                                        Meta.builder().value("file", entry.getKey().getPath().toLowerCase()).build());
                                System.out.println("Loaded commands: " + entry.getKey().getPath());
                            } catch (CommandException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onUpdated(List<File> files) {
                        onRemoved(files);
                        onAdded(files);
                    }

                    @Override
                    public void onRemoved(List<File> files) {
                        files.forEach(file -> manager.unregisterCommands(meta -> meta.is("file", file.getPath().toLowerCase())));
                    }

                    private Map<File, Object> loadScripts(List<File> files) {
                        Map<File, Object> scripts = new HashMap<>();

                        CountDownLatch latch = new CountDownLatch(files.size());
                        for (File file : files) {
                            service.submit(() -> {
                                try {
                                    Object obj = loader.file(file).expectClass().constructor().get().instance().object();
                                    scripts.put(file, obj);
                                } catch (Exception e) {
                                    System.err.println("Unable to load script: " + file.getPath());
                                } finally {
                                    latch.countDown();
                                }
                            });
                        }
                        try {
                            latch.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        return scripts;
                    }

                }).start();
    }
}
