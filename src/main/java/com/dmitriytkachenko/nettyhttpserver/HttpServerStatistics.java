package com.dmitriytkachenko.nettyhttpserver;

import io.netty.channel.Channel;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.javatuples.Pair;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/* One pipeline per connection. Different pipelines can be run in different threads from worker thread pool.
* Because of that, implementation of this class is thread-safe.
* */
public enum HttpServerStatistics implements Serializable {
    INSTANCE;

    private ConcurrentHashMap<String, Pair<Long, LocalDateTime>> ipRequests = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Long> redirects = new ConcurrentHashMap<>();
    private List<ConnectionInfo> connections = Collections.synchronizedList(new ArrayList<>());

    /* Holds references to open channels (they remove themselves when they are closed). */
    private DefaultChannelGroup channels  = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public long getNumberOfRequests() {
        System.err.println(ipRequests != null);
        return ipRequests != null && ipRequests.size() != 0 ? ipRequests.reduceValues(1, Pair::getValue0, Long::sum) : 0;
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

    public static String getIpFromChannel(Channel channel) {
        return ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress().replaceFirst("^/", "");
    }

    public static String getFormattedDateTime(LocalDateTime ldt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS dd.MM.yyyy");
        return ldt.format(formatter);
    }

    public List<List<String>> getIpRequestsAsStrings() {
        List<List<String>> list = new ArrayList<>();
        ipRequests.forEach((ip, info) -> {
            List<String> s = Arrays.asList(ip, info.getValue0().toString(), getFormattedDateTime(info.getValue1()));
            list.add(s);
        });
        return list;
    }

    public List<List<String>> getRedirectsAsStrings() {
        List<List<String>> list = new ArrayList<>();
        redirects.forEach((url, count) -> {
            List<String> s = Arrays.asList(url, count.toString());
            list.add(s);
        });
        return list;
    }

    public List<List<String>> getConnectionsAsStrings() {
        List<List<String>> list = new ArrayList<>();
        connections.forEach((ci) -> {
            List<String> s = new ArrayList<>();
            s.add(ci.getIp());
            s.add(ci.getUrisAsString());
            s.add(getFormattedDateTime(ci.getEstablished()));
            if (ci.getClosed() != null) {
                s.add(getFormattedDateTime(ci.getClosed()));
                s.add("" + ci.getBytesSent());
                s.add("" + ci.getBytesReceived());
                s.add(String.format("%.3f", ci.getSpeed()));
            } else {
                s.add("–"); s.add("–"); s.add("–"); s.add("–");
            }
            list.add(s);
        });
        return list;
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

    public synchronized void addConnectionInfo(ConnectionInfo ci) {
        if (connections.size() == 16) {
            connections.remove(0);
        }
        connections.add(ci);
        System.err.println("ConnectionInfo added.");
        System.err.println("Connections: " + connections.size());
    }

    public List<ConnectionInfo> getConnections() {
        return connections;
    }
}
