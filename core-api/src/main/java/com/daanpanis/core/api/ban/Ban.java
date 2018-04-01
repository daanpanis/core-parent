package com.daanpanis.core.api.ban;

import java.util.Date;
import java.util.UUID;

public class Ban {

    private final UUID userId;
    private final UUID bannerId;
    private final Date bannedDate;
    private final boolean active;
    private final String reason;

    public Ban(UUID userId, UUID bannerId, Date bannedDate, boolean active, String reason) {
        this.userId = userId;
        this.bannerId = bannerId;
        this.bannedDate = bannedDate;
        this.active = active;
        this.reason = reason;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getBannerId() {
        return bannerId;
    }

    public Date getBannedDate() {
        return bannedDate;
    }

    public boolean isActive() {
        return active;
    }

    public String getReason() {
        return reason;
    }
}
