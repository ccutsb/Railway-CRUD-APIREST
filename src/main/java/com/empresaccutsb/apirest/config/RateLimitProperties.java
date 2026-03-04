package com.empresaccutsb.apirest.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.rate-limit.login")
public class RateLimitProperties {

    @Min(1)
    private long capacity;

    @Min(1)
    private long refillTokens;

    @Min(1)
    private long refillMinutes;

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public long getRefillTokens() {
        return refillTokens;
    }

    public void setRefillTokens(long refillTokens) {
        this.refillTokens = refillTokens;
    }

    public long getRefillMinutes() {
        return refillMinutes;
    }

    public void setRefillMinutes(long refillMinutes) {
        this.refillMinutes = refillMinutes;
    }
}
