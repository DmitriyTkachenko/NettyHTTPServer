package com.dmitriytkachenko.nettyhttpserver;

import java.io.Serializable;
import java.time.LocalDateTime;

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
    private String uri;
    private long bytesSent;
    private long bytesReceived;

    public ConnectionInfo() {
        connectionId = count;
        ++count;
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

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public long getBytesSent() {
        return bytesSent;
    }

    public void setBytesSent(long bytesSent) {
        this.bytesSent = bytesSent;
    }
}
