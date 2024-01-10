package ru.itskekoff.hackchecker.bot.utils.types;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public @Data class BannedUser {
    private long id;
    private long expiration;
    private String reason;

    public boolean isExpired() {
        return System.currentTimeMillis() >= expiration;
    }

    public BigInteger expirationToSeconds() {
        return BigInteger.valueOf((expiration - System.currentTimeMillis()) / 1000);
    }
}
