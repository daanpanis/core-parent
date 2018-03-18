package com.daanpanis.core.api.command.permission;

import com.daanpanis.core.api.command.PermissionHandler;
import org.bukkit.command.CommandSender;

public class DefaultPermissionHandler implements PermissionHandler<Permission> {

    @Override
    public boolean hasPermission(CommandSender sender, Permission annotation) {
        return sender.hasPermission(annotation.node());
    }
}
