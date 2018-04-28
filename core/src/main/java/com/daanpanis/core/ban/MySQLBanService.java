package com.daanpanis.core.ban;

import com.daanpanis.core.api.ban.Ban;
import com.daanpanis.core.api.ban.BanService;
import com.daanpanis.core.api.ban.TempBan;
import com.daanpanis.database.mysql.MySQL;
import com.daanpanis.database.mysql.QueryResult;
import com.daanpanis.injection.Inject;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MySQLBanService implements BanService {

    private static final String TABLE = "bans";

    @Inject
    private MySQL mysql;

    @Override
    public CompletableFuture<Ban> getActiveBan(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Ban ban = mysql.query("SELECT * FROM bans WHERE uuid=? AND active=1;", playerId.toString()).stream().map(MySQLBanService::toBan)
                        .findFirst().orElse(null);
                if (ban != null && ban instanceof TempBan && !ban.isActive()) {
                    unban(ban.getUserId());
                    return null;
                }
                return ban;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Collection<Ban>> getAllBans(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return mysql.query("SELECT * FROM " + TABLE + " WHERE uuid=?;", playerId.toString()).stream().map(MySQLBanService::toBan)
                        .collect(Collectors.toList());
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    @Override
    public CompletableFuture unban(UUID playerId) {
        return CompletableFuture.runAsync(() -> {
            try {
                mysql.update("UPDATE " + TABLE + " SET active=NULL WHERE uuid=? AND active=1;", playerId.toString());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public CompletableFuture addBan(Ban ban) {
        return CompletableFuture.runAsync(() -> {
            try {
                Date endDate = ban instanceof TempBan ? ((TempBan) ban).getEndDate() : null;
                mysql.update("INSERT INTO " + TABLE + "(uuid, banner, banned_date, end_date, active, reason) VALUES (?, ?, ?, ?, ?, ?);",
                        ban.getUserId().toString(), ban.getBannerId().toString(), ban.getBannedDate(), endDate, true, ban.getReason());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    protected static Ban toBan(QueryResult.ResultData data) {
        UUID uuid = UUID.fromString(data.asString("uuid"));
        boolean active = data.asBoolean("active");
        UUID banner = UUID.fromString(data.asString("banner"));
        Date bannedDate = data.asDate("banned_date");
        Date endDate = data.asDate("end_date");
        String reason = data.asString("reason");
        if (endDate != null)
            return new TempBan(uuid, banner, bannedDate, active, reason, endDate);
        return new Ban(uuid, banner, bannedDate, active, reason);
    }
}
