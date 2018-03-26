package com.daanpanis.core.command;


import com.daan.scripting.loading.impl.ScriptLoaderImpl;
import com.daanpanis.core.api.command.CommandManager;
import com.daanpanis.core.api.command.exceptions.CommandException;
import com.daanpanis.core.api.command.meta.Meta;
import com.daanpanis.core.api.command.meta.MetaTags;
import com.daanpanis.filewatcher.TrackedFile;
import com.daanpanis.filewatcher.UpdateHandler;
import com.daanpanis.filewatcher.github.Async;
import com.daanpanis.injection.DependencyInjector;
import com.daanpanis.injection.Inject;
import com.daanpanis.scripting.loading.api.ScriptLoader;
import com.daanpanis.scripting.loading.groovy.GroovyCompiler;
import groovy.lang.GroovySystem;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public class CommandScriptHandler implements UpdateHandler {

    @Inject
    private DependencyInjector injector;
    @Inject
    private CommandManager commandManager;

    private final ScriptLoader loader = new ScriptLoaderImpl(new GroovyCompiler());

    @Override
    public void onUpdated(Collection<? extends TrackedFile> files) {
        onRemoved(files);
        onAdded(files);
    }

    @Override
    public void onRemoved(Collection<? extends TrackedFile> files) {
        List<Class<?>> unregister = new ArrayList<>();
        files.stream().filter(file -> file.getExtension().equalsIgnoreCase("groovy")).forEach(file -> commandManager.unregisterCommands(meta -> {
            if (!meta.is(MetaTags.FILE, getFullFileName(file))) return false;
            if (meta.has(MetaTags.SCRIPT)) {
                unregister.add(meta.get(MetaTags.SCRIPT));
                for (Class<?> groovyClass : GroovyCompiler.classLoader.getLoadedClasses()) {
                    if (groovyClass.toString().equals(meta.get(MetaTags.SCRIPT))) {
                        unregister.add(groovyClass);
                    }
                }
            }
            return true;
        }));
        unregister.forEach(cls -> GroovySystem.getMetaClassRegistry().removeMetaClass(cls));
        GroovyCompiler.classLoader.clearCache();
    }

    @Override
    public void onAdded(Collection<? extends TrackedFile> files) {
        for (Map.Entry<TrackedFile, Object> entry : loadScripts(
                files.stream().filter(file -> file.getExtension().equalsIgnoreCase("groovy")).collect(Collectors.toList())).entrySet()) {
            try {
                commandManager.registerCommands(entry.getValue(),
                        Meta.builder().value(MetaTags.FILE, getFullFileName(entry.getKey())).value(MetaTags.SCRIPT, entry.getValue().getClass())
                                .build());
                System.out.println("Loaded commands: " + getFullFileName(entry.getKey()));
            } catch (CommandException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String[] getNames() {
        return new String[]{"command", "commands"};
    }

    private String getFullFileName(TrackedFile file) {
        return file.getBase() + file.getPath() + file.getName();
    }

    private Map<TrackedFile, Object> loadScripts(Collection<? extends TrackedFile> files) {
        Map<TrackedFile, Object> scripts = new HashMap<>();
        try {
            Async.runParallel(files.stream().map(file -> (Runnable) () -> {
                try {
                    scripts.put(file, injector.inject(loader.stream(file.getInput()).expectClass().get()));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Unable to load script: " + getFullFileName(file));
                    // TODO Log
                }
            }).collect(Collectors.toList())).await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

      /*  CompletableFuture.allOf(files.stream().map(file -> CompletableFuture.runAsync(() -> {
            try {
                scripts.put(file, injector.inject(loader.stream(file.getInput()).expectClass().get()));
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Unable to load script: " + getFullFileName(file));
                // TODO Log
            }
        })).toArray((IntFunction<CompletableFuture<?>[]>) CompletableFuture[]::new)).join();*/
        return scripts;
    }
}
