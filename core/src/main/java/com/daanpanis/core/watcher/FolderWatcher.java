package com.daanpanis.core.watcher;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FolderWatcher {

    private final Collection<File> folders = new HashSet<>();
    private final Map<String, Date> trackedFiles = new HashMap<>();
    private boolean running = false;
    private UpdateHandler handler;
    private FileFilter filter;


    public FolderWatcher addFolder(String folder) {
        return addFolder(new File(folder));
    }

    public FolderWatcher addFolder(File folder) {
        Preconditions.checkNotNull(folder);
        Preconditions.checkArgument(folder.isDirectory(), "The file must be a directory");
        Preconditions.checkArgument(folder.exists(), "This folder doesn't exist");
        folders.add(folder);
        return this;
    }

    public FolderWatcher setHandler(UpdateHandler handler) {
        this.handler = handler;
        return this;
    }

    public FolderWatcher setFilter(FileFilter filter) {
        this.filter = filter;
        return this;
    }

    public void start() {
        if (!running) {
            running = true;
            new Thread(() -> {
                while (running) {
                    Map<String, Date> old = new HashMap<>(trackedFiles);
                    trackedFiles.clear();
                    folders.forEach(folder -> {
                        File[] files = folder.listFiles(file -> !file.isDirectory() && (filter == null || filter.accept(file)));

                        if (files != null)
                            for (File file : files) {
                                trackedFiles.put(file.getPath().toLowerCase(), new Date(file.lastModified()));
                            }
                        if (files != null && handler != null) {
                            List<File> added = getAddedFiles(files, old);
                            List<File> removed = getRemovedFiles(files, old);
                            if (!removed.isEmpty()) {
                                System.out.println(added.size() + " removed files found");
                                handler.onRemoved(removed);
                            }
                            List<File> updated = getUpdatedFiles(files, old);
                            if (!updated.isEmpty()) {
                                System.out.println(updated.size() + " updated files found");
                                handler.onUpdated(updated);
                            }
                            if (!added.isEmpty()) {
                                System.out.println(added.size() + " newly added files found");
                                handler.onAdded(added);
                            }
                        }
                    });
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private List<File> getRemovedFiles(File[] files, Map<String, Date> trackedFiles) {
        List<File> removed = new ArrayList<>();
        Set<String> fileNames = Stream.of(files).map(file -> file.getPath().toLowerCase()).collect(Collectors.toSet());
        for (String fileName : trackedFiles.keySet()) {
            if (!fileNames.contains(fileName))
                removed.add(new File(fileName));
        }
        return removed;
    }

    private List<File> getUpdatedFiles(File[] files, Map<String, Date> trackedFiles) {
        List<File> updated = new ArrayList<>();
        for (File file : files) {
            String path = file.getPath().toLowerCase();
            if (trackedFiles.containsKey(path)) {
                Date lastUpdated = trackedFiles.get(path);
                if (new Date(file.lastModified()).after(lastUpdated))
                    updated.add(file);
            }
        }
        return updated;
    }

    private List<File> getAddedFiles(File[] files, Map<String, Date> trackedFiles) {
        List<File> added = new ArrayList<>();
        for (File file : files) {
            if (!trackedFiles.containsKey(file.getPath().toLowerCase()))
                added.add(file);
        }
        return added;
    }

    void stop() {
        running = false;
    }

}
