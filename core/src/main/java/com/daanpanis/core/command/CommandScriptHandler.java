package com.daanpanis.core.command;


import com.daanpanis.core.api.command.CommandManager;
import com.daanpanis.core.api.command.exceptions.CommandException;
import com.daanpanis.core.api.command.meta.Meta;
import com.daanpanis.core.api.command.meta.MetaTags;
import com.daanpanis.core.watcher.ScriptUpdateHandler;
import com.daanpanis.filewatcher.TrackedFile;
import com.daanpanis.injection.DependencyInjector;
import com.daanpanis.injection.Inject;
import com.daanpanis.scripting.loading.api.exception.ScriptException;
import com.daanpanis.scripting.loading.groovy.GroovyCompiler;
import groovy.lang.GroovySystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommandScriptHandler extends ScriptUpdateHandler {

    @Inject
    private DependencyInjector injector;
    @Inject
    private CommandManager commandManager;

    public CommandScriptHandler() {
        super(new GroovyCompiler());
    }

    @Override
    public void onUpdated(Collection<? extends TrackedFile> files) {
        onRemoved(files);
        onAdded(files);
    }

    @Override
    public void onRemoved(Collection<? extends TrackedFile> files) {
        List<Class<?>> unregister = new ArrayList<>();
        files.stream().filter(file -> file.getExtension().equalsIgnoreCase("groovy")).forEach(file -> commandManager.unregisterCommands(meta -> {
            if (!meta.is(MetaTags.FILE, file.getFullFileName().toString()))
                return false;
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

    @SuppressWarnings("unchecked")
    @Override
    public void onAdded(Collection<? extends TrackedFile> files) {
        loadScripts(files, reader -> injector.inject(reader.expectClass().get()), "groovy").forEach((file, script) -> {
            try {
                commandManager.registerCommands(script,
                        Meta.builder().value(MetaTags.FILE, file.getFullFileName().toString()).value(MetaTags.SCRIPT, script.getClass()).build());
                System.out.println("Loaded commands: " + file.getFullFileName());
            } catch (CommandException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public String[] getNames() {
        return new String[]{"command", "commands"};
    }
}
