package com.daanpanis.corebungee;

import com.daanpanis.corebungee.api.CoreBungee;
import net.md_5.bungee.api.plugin.Plugin;

public class CoreBungeePlugin extends Plugin {

    @Override
    public void onEnable() {
        CoreBungee.setApi(new CoreBungeeApiImpl());
    }

    @Override
    public void onDisable() {

    }
}
