package com.daanpanis.core.watcher;

import java.io.File;
import java.util.List;

public interface UpdateHandler {

    void onAdded(List<File> files);

    void onUpdated(List<File> files);

    void onRemoved(List<File> files);

}
