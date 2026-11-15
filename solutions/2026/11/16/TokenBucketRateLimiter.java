import java.util.function.LongSupplier;

/**
 * Token bucket rate limiter. Tokens refill continuously at a fixed rate up to a burst capacity;
 * each request spends tokens or is rejected. The clock is injected so behaviour is testable,
 * and methods are synchronized for thread safety.
 */
public class TokenBucketRateLimiter {
    private final double capacity;
    private final double refillPerSecond;
    private final LongSupplier nanoClock;
    private double tokens;
    private long lastRefill;

    TokenBucketRateLimiter(double capacity, double refillPerSecond, LongSupplier nanoClock) {
        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
        this.nanoClock = nanoClock;
        this.tokens = capacity;
        this.lastRefill = nanoClock.getAsLong();
    }

    synchronized boolean tryAcquire(int permits) {
        refill();
        if (tokens >= permits) {
            tokens -= permits;
            return true;
        }
        return false;
    }

    private void refill() {
        long now = nanoClock.getAsLong();
        double elapsedSeconds = (now - lastRefill) / 1_000_000_000.0;
        tokens = Math.min(capacity, tokens + elapsedSeconds * refillPerSecond);
        lastRefill = now;
    }

    public static void main(String[] args) {
        long[] now = {0};
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(5, 1, () -> now[0]);

        for (int i = 0; i < 5; i++) if (!limiter.tryAcquire(1)) throw new AssertionError("burst " + i);
        if (limiter.tryAcquire(1)) throw new AssertionError("6th request should be rejected");

        now[0] += 2_000_000_000L;           // 2 seconds pass -> 2 tokens
        if (!limiter.tryAcquire(1) || !limiter.tryAcquire(1)) throw new AssertionError("refill");
        if (limiter.tryAcquire(1)) throw new AssertionError("no tokens left");

        now[0] += 60_000_000_000L;          // a long idle period never exceeds capacity
        if (!limiter.tryAcquire(5)) throw new AssertionError("full burst after idle");
        if (limiter.tryAcquire(1)) throw new AssertionError("capacity cap");
        System.out.println("Token bucket allows bursts, refills over time and caps at capacity.");
    }
}
