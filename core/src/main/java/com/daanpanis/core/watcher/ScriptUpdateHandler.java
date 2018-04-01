package com.daanpanis.core.watcher;

import com.daan.scripting.loading.impl.ScriptLoaderImpl;
import com.daanpanis.filewatcher.TrackedFile;
import com.daanpanis.filewatcher.UpdateHandler;
import com.daanpanis.filewatcher.github.Async;
import com.daanpanis.scripting.loading.api.ScriptCompiler;
import com.daanpanis.scripting.loading.api.ScriptLoader;
import com.daanpanis.scripting.loading.api.ScriptReader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ScriptUpdateHandler implements UpdateHandler {

    protected final ScriptLoader loader;

    public ScriptUpdateHandler(ScriptCompiler compiler) {
        this.loader = new ScriptLoaderImpl(compiler);
    }

    protected <T> Map<TrackedFile, T> loadScripts(Collection<TrackedFile> files, ScriptTransform<ScriptReader, T> transform) {
        return loadScripts(files, transform, null);
    }

    protected <T> Map<TrackedFile, T> loadScripts(Collection<? extends TrackedFile> files, ScriptTransform<ScriptReader, T> transform,
            String extension) {
        Map<TrackedFile, T> scripts = new HashMap<>();
        try {
            Async.runParallel(
                    files.stream().filter(file -> extension == null || file.getExtension().equalsIgnoreCase(extension)).map(file -> (Runnable) () -> {
                        try {
                            T script = transform.apply(loader.stream(file.getInput()));
                            scripts.put(file, script);
                        } catch (Exception e) {
                            e.printStackTrace();
                            // TODO Log
                            System.err.println("Unable to load: " + file.getFullFileName());
                        }
                    }).collect(Collectors.toList())).await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return scripts;
    }

}
