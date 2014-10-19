package com.dmitriytkachenko.nettyhttpserver;

import io.netty.channel.Channel;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.javatuples.Pair;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public enum HttpServerStatistics {
    INSTANCE;

    private AtomicLong numberOfRequests = new AtomicLong(0);
    private ConcurrentHashMap<String, Pair<Long, LocalDateTime>> ipRequests = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Long> redirects = new ConcurrentHashMap<>();
    private DefaultChannelGroup channels  = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public void incrementNumberOfRequests() {
        numberOfRequests.incrementAndGet();
    }

    public long getNumberOfRequests() {
        return numberOfRequests.get();
    }

    public synchronized void registerRequestFromIp(String ip, LocalDateTime localDateTime) {
        if (ipRequests.containsKey(ip)) {
            ipRequests.put(ip, Pair.with(ipRequests.get(ip).getValue0() + 1, localDateTime));
        } else {
            ipRequests.put(ip, Pair.with(1L, localDateTime));
        }
    }

    public synchronized void registerRedirect(String destinationUrl) {
        if (redirects.containsKey(destinationUrl)) {
            redirects.put(destinationUrl, redirects.get(destinationUrl) + 1);
        } else {
            redirects.put(destinationUrl, 1L);
        }
    }

    public long getNumberOfUniqueRequests() {
        return ipRequests.size();
    }

    public void addChannel(Channel c) {
        channels.add(c);
    }

    public int getConnectionCount() {
        return channels.size();
    }

}
