package com.daanpanis.corebungee.api;

import com.google.common.base.Preconditions;

public class CoreBungee {

    private static CoreBungeeApi instance;

    public static void setApi(CoreBungeeApi api) {
        Preconditions.checkNotNull(api, "The api instance can't be null!");
        instance = api;
    }

    public static CoreBungeeApi getApi() {
        return instance;
    }

    private CoreBungee() {
    }

}
