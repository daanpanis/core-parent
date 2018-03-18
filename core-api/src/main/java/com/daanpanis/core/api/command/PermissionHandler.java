package com.daanpanis.core.api.command;

import org.bukkit.command.CommandSender;

import java.lang.annotation.Annotation;

public interface PermissionHandler<T extends Annotation> {

    boolean hasPermission(CommandSender sender, T annotation);

}
