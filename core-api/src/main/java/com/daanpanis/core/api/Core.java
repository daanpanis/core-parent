package com.daanpanis.core.api;

import com.google.common.base.Preconditions;

public class Core {

    private static CoreApi instance;

    public static void setApi(CoreApi api) {
        Preconditions.checkNotNull(api, "The api instance can't be null!");
        instance = api;
    }

    public static CoreApi getApi() {
        return instance;
    }

    private Core() {
    }

}
