package com.daanpanis.core.api.ban;

import java.util.Date;
import java.util.UUID;

public class TempBan extends Ban {

    private final Date endDate;

    public TempBan(UUID userId, UUID bannerId, Date bannedDate, boolean active, String reason, Date endDate) {
        super(userId, bannerId, bannedDate, active, reason);
        this.endDate = endDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    @Override
    public boolean isActive() {
        return super.isActive() && endDate.after(new Date());
    }
}
