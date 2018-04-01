package com.daanpanis.core.listener;

import com.daanpanis.core.api.CoreApi;
import com.daanpanis.core.watcher.ScriptUpdateHandler;
import com.daanpanis.filewatcher.TrackedFile;
import com.daanpanis.injection.DependencyInjector;
import com.daanpanis.injection.Inject;
import com.daanpanis.scripting.loading.groovy.GroovyCompiler;
import groovy.lang.GroovySystem;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ListenerScriptHandler extends ScriptUpdateHandler {

    @Inject
    private DependencyInjector injector;
    @Inject
    private CoreApi api;

    private final Map<String, Listener> registeredListeners = new HashMap<>();

    public ListenerScriptHandler() {
        super(new GroovyCompiler());
    }

    @Override
    public void onUpdated(Collection<? extends TrackedFile> collection) {
        onRemoved(collection);
        onAdded(collection);
    }

    @Override
    public void onRemoved(Collection<? extends TrackedFile> collection) {
        collection.stream().filter(file -> file.getExtension().equalsIgnoreCase("groovy")).forEach(file -> {
            String fileName = file.getFullFileName().toString();
            if (registeredListeners.containsKey(fileName)) {
                Listener listener = registeredListeners.remove(fileName);
                HandlerList.unregisterAll(listener);
                GroovySystem.getMetaClassRegistry().removeMetaClass(listener.getClass());
            }
        });
        GroovyCompiler.classLoader.clearCache();
    }

    @Override
    public void onAdded(Collection<? extends TrackedFile> collection) {
        loadScripts(collection, reader -> injector.inject(reader.expectClass().inherits(Listener.class).cast(Listener.class).get()), "groovy")
                .forEach((file, listener) -> {
                    registeredListeners.put(file.getFullFileName().toString(), listener);
                    api.getPlugin().getServer().getPluginManager().registerEvents(listener, api.getPlugin());
                    System.out.println("Loaded listener: " + file.getFullFileName());
                });
    }

    @Override
    public String[] getNames() {
        return new String[]{"listener", "events"};
    }
}
