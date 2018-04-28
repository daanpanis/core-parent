package com.daanpanis.core.ban;

import com.daanpanis.core.api.ban.Ban;
import com.daanpanis.core.api.ban.BanService;
import com.daanpanis.core.api.ban.TempBan;
import com.daanpanis.database.mysql.MySQL;
import com.daanpanis.injection.Inject;
import com.ivanceras.fluent.sql.SQL;
import org.jooq.lambda.Unchecked;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.ivanceras.fluent.sql.SQL.Statics.*;

public class MySQLFluentBanService implements BanService {

    private static final String TABLE = "bans";

    @Inject
    private MySQL mysql;

    @Override
    public CompletableFuture<Ban> getActiveBan(UUID playerId) {
        return CompletableFuture.supplyAsync(Unchecked.supplier(
                () -> mysql.query(
                        SELECT("*").FROM(TABLE)
                                .WHERE("uuid").EQUAL_TO(playerId.toString())
                                .AND("active").EQUAL(true))
                        .stream().map(MySQLBanService::toBan)
                        .findFirst().orElse(null)));
    }

    @Override
    public CompletableFuture<Collection<Ban>> getAllBans(UUID playerId) {
        return CompletableFuture.supplyAsync(Unchecked.supplier(
                () -> mysql.query(
                        SELECT("*").FROM(TABLE)
                                .WHERE("uuid").EQUAL_TO(playerId.toString()))
                        .stream().map(MySQLBanService::toBan)
                        .collect(Collectors.toList())));
    }

    @Override
    public CompletableFuture unban(UUID playerId) {
        return CompletableFuture.runAsync(Unchecked.runnable(
                () -> mysql.update(
                        UPDATE(TABLE).SET("active", null)
                                .WHERE("uuid").EQUAL_TO(playerId.toString())
                                .AND("active").IS_NOT_NULL())));
    }

    @Override
    public CompletableFuture addBan(Ban ban) {
        return CompletableFuture.runAsync(Unchecked.runnable(() -> {
            Date endDate = ban instanceof TempBan ? ((TempBan) ban).getEndDate() : null;
            SQL sql = INSERT().INTO(TABLE).openParen()
                    .FIELD("uuid", "banner", "banned_date", "end_date", "active", "reason").closeParen()
                    .VALUES().openParen()
                    .VALUE(ban.getUserId().toString()).comma()
                    .VALUE(ban.getBannerId().toString()).comma()
                    .VALUE(new Date()).comma()
                    .VALUE(endDate).comma()
                    .VALUE(true).comma()
                    .VALUE(ban.getReason())
                    .closeParen();
            mysql.update(sql);
        }));
    }
}
