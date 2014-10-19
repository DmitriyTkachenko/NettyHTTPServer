package com.dmitriytkachenko.nettyhttpserver;

import io.netty.channel.Channel;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.javatuples.Pair;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/* One pipeline per connection. Different pipelines can be run in different threads from worker thread pool.
* Because of that, implementation of this class is thread-safe.
* */
public enum HttpServerStatistics implements Serializable {
    INSTANCE;

    private AtomicLong numberOfRequests = new AtomicLong(0);
    private ConcurrentHashMap<String, Pair<Long, LocalDateTime>> ipRequests = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Long> redirects = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, ConnectionInfo> connections = new ConcurrentHashMap<>();

    /* Holds references to open channels (they remove themselves when they are closed). */
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

    public ConnectionInfo getConnectionInfoForId(Long id) {
        System.err.println("Connections: " + connections.size());
        return connections.get(id);
    }

    public void addConnectionInfo(ConnectionInfo ci) {
        connections.put(ci.getConnectionId(), ci);
        System.err.println("ConnectionInfo added.");
        System.err.println("Connections: " + connections.size());
    }

    public void setUriForConnectionInfoWithId(String uri, Long id) {
        connections.get(id).setUri(uri);
        System.err.println("Uri for connectionInfo set.");
        System.err.println("Connections: " + connections.size());
    }
}
