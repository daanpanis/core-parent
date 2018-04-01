package com.daanpanis.core.api.ban;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface BanService {

    CompletableFuture<Ban> getActiveBan(UUID playerId);

    CompletableFuture<Collection<Ban>> getAllBans(UUID playerId);

    CompletableFuture unban(UUID playerId);

    CompletableFuture addBan(Ban ban);

}
