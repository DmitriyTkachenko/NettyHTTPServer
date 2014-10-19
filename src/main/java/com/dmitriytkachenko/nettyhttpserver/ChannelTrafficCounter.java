package com.dmitriytkachenko.nettyhttpserver;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;

import java.time.LocalDateTime;

public class ChannelTrafficCounter extends ChannelTrafficShapingHandler {
    private final long connectionId;

    public ChannelTrafficCounter(long checkInterval, long connectionId) {
        super(checkInterval);
        this.connectionId = connectionId;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        this.trafficCounter().start();
    }

    @Override
    public synchronized void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        this.trafficCounter().stop();
        ConnectionInfo ci = HttpServerStatistics.INSTANCE.getConnectionInfoForId(connectionId);
        ci.setClosed(LocalDateTime.now());
        ci.setBytesReceived(this.trafficCounter().cumulativeReadBytes());
        ci.setBytesSent(this.trafficCounter().cumulativeWrittenBytes());
    }

}
