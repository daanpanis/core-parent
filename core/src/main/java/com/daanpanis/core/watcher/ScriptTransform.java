package com.daanpanis.core.watcher;

import com.daanpanis.scripting.loading.api.exception.ScriptException;

public interface ScriptTransform<T, V> {

    V apply(T value) throws InterruptedException, ScriptException;

}
