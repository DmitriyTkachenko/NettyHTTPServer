package com.dmitriytkachenko.nettyhttpserver;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.time.LocalDateTime;

public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel sc) throws Exception {
        ConnectionInfo ci = new ConnectionInfo();
        ci.setIp(HttpServerStatistics.getIpFromChannel(sc));
        ci.setEstablished(LocalDateTime.now());
        HttpServerStatistics.getInstance().addConnectionInfo(ci);
        ChannelPipeline cp = sc.pipeline();
        cp.addLast(new ChannelTrafficCounter(0, ci));
        cp.addLast(new HttpRequestDecoder());
        cp.addLast(new HttpResponseEncoder());
        cp.addLast(new HttpContentCompressor());
        cp.addLast(new HttpServerHandler(ci));
    }
}
