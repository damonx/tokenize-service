package nz.co.anz.tokenization.properties;

import nz.co.anz.tokenization.validators.PositiveDuration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import jakarta.validation.constraints.Positive;

/**
 * Cache related properties.
 */
@Component
@ConfigurationProperties(prefix = "tokenization.cache")
@Validated
public class TokenCacheProperties
{
    @PositiveDuration
    @DurationUnit(ChronoUnit.MINUTES)
    private Duration ttl;

    @Positive
    private int maximumSize;

    @Positive
    private int initialCapacity;

    public Duration getTtl()
    {
        return ttl;
    }

    public void setTtl(final Duration ttl)
    {
        this.ttl = ttl;
    }

    public int getMaximumSize()
    {
        return maximumSize;
    }

    public void setMaximumSize(final int maximumSize)
    {
        this.maximumSize = maximumSize;
    }

    public int getInitialCapacity()
    {
        return initialCapacity;
    }

    public void setInitialCapacity(final int initialCapacity)
    {
        this.initialCapacity = initialCapacity;
    }
}
