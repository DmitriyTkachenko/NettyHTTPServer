package com.dmitriytkachenko.nettyhttpserver;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/* One instance of this class per pipeline.
Netty guarantees that a given pipeline instance is always called back from the same worker thread
(unless there are executors in pipeline).
So this class does not have to be thread-safe.
*/
public class ConnectionInfo implements Serializable {
    private static long count = 0;
    private final long connectionId;
    private LocalDateTime established;
    private LocalDateTime closed;
    private String ip;

    /* Client can visit multiple URIs using one connection in case it is using HTTP keep-alive (which most browsers do). */
    private Set<String> uris;

    private long bytesSent;
    private long bytesReceived;

    public ConnectionInfo() {
        connectionId = count;
        ++count;
        uris = new HashSet<>();
    }

    public String getUrisAsString() {
        StringBuilder sb = new StringBuilder();
        uris.forEach((uri) -> sb.append(uri).append(", "));
        if (sb.length() > 1) {
            sb.delete(sb.length() - 2, sb.length());
        }
        return sb.toString();
    }

    public double getSpeed() {
        double connectionDuration = ChronoUnit.MILLIS.between(established, closed);
        connectionDuration /= 1000; // to seconds
        /* Round to 3 decimal places. Sent and received bytes are divided separately to prevent overflow. */
        double sentSpeed = Math.round((bytesSent / connectionDuration) * 1000) / 1000;
        double receivedSpeed = Math.round((bytesReceived / connectionDuration) * 1000) / 1000;
        return sentSpeed + receivedSpeed;
    }

    public long getConnectionId() {
        return connectionId;
    }

    public long getBytesReceived() {
        return bytesReceived;
    }

    public void setBytesReceived(long bytesReceived) {
        this.bytesReceived = bytesReceived;
    }

    public LocalDateTime getEstablished() {
        return established;
    }

    public void setEstablished(LocalDateTime established) {
        this.established = established;
    }

    public LocalDateTime getClosed() {
        return closed;
    }

    public void setClosed(LocalDateTime closed) {
        this.closed = closed;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Set<String> getUris() {
        return uris;
    }

    public void addUri(String uri) {
        if (uri != null) {
            uris.add(uri);
        }
    }

    public long getBytesSent() {
        return bytesSent;
    }

    public void setBytesSent(long bytesSent) {
        this.bytesSent = bytesSent;
    }
}
