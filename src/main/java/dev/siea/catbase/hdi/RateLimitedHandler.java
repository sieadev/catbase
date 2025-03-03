package dev.siea.catbase.hdi;


import com.pixelservices.flash.lifecycle.Request;
import com.pixelservices.flash.lifecycle.Response;
import dev.siea.catbase.db.models.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class RateLimitedHandler extends RBACAuthHandler {
    private static final Map<String, Integer> requestCounts = new ConcurrentHashMap<>();
    private static final Map<String, Long> requestTimeStamps = new ConcurrentHashMap<>();

    private String identifier;
    private int maxRequests;
    private long timeWindowMillis;

    public RateLimitedHandler(Request req, Response res) {
        super(req, res);
    }

    @Override
    protected final Object handleRBAC() {
        String ip = req.clientAddress().getAddress().getHostAddress().toString();
        this.identifier = userRole.equals(User.Role.GUEST) ? ip : apiKey;

        this.maxRequests = userRole.getMaxRequests();
        this.timeWindowMillis = userRole.getTimeWindowMillis();

        long currentTime = System.currentTimeMillis();
        int currentCount = requestCounts.getOrDefault(identifier, 0);
        long lastReset = requestTimeStamps.getOrDefault(identifier, currentTime);

        if (currentTime - lastReset > timeWindowMillis) {
            requestCounts.put(identifier, 1);
            requestTimeStamps.put(identifier, currentTime);
        } else {
            if (currentCount >= maxRequests) {
                res.status(429);
                return "{\"error\":\"Rate limit exceeded\"}";
            }
            requestCounts.put(identifier, currentCount + 1);
        }

        return handleRateLimited();
    }

    protected abstract Object handleRateLimited();
}
