package com.dmitriytkachenko.nettyhttpserver;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpServerHandler extends SimpleChannelInboundHandler<Object> {
    private HttpRequest request;
    /* Buffer that stores the response content */
    private final StringBuilder responseBuffer = new StringBuilder();
    private final HttpServerStatistics statistics = HttpServerStatistics.getInstance();
    private final ConnectionInfo ci;

    public HttpServerHandler(ConnectionInfo ci) {
        this.ci = ci;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        statistics.addChannel(ctx.channel());
    }

    class UrlMapper {
        private HttpMethod method;
        private String[] pathComponents;
        private Map<String, List<String>> params;
        private ChannelHandlerContext ctx;

        public UrlMapper(ChannelHandlerContext ctx) {
            method = request.getMethod();
            pathComponents = request.getUri().replaceFirst("^/", "").split("/"); // remove first slash and split
            pathComponents[pathComponents.length - 1] = pathComponents[pathComponents.length - 1].split("\\?")[0]; // remove attributes from the last component
            params = new QueryStringDecoder(request.getUri()).parameters();
            this.ctx = ctx;
        }

        public void callAppropriateMethod() {

            if (method != HttpMethod.GET) {
                send501NotImplemented(ctx);
                return;
            }

            if (pathComponents.length == 1) {
                /* Show "hello" page at http://domain/hello or http://domain/hello/ */
                if (pathComponents[0].equals("hello") && params.size() == 0) {
                    serveHelloPage(ctx);
                    return;
                }

                /* Show "status" page at http://domain/status or http://domain/status/ */
                if (pathComponents[0].equals("status") && params.size() == 0) {
                    serveStatusPage(ctx);
                    return;
                }

                /* Redirect to http://<url> at http://domain/redirect?url=<url>
                Example: http://domain/redirect?url=google.com will redirect to http://google.com
                 */
                if (pathComponents[0].equals("redirect") && params.size() == 1 && params.get("url").size() == 1) {
                    sendRedirect(ctx, params.get("url").get(0));
                    return;
                }
            }

            send404NotFound(ctx);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
        /* Writing response to request is over. Record request in statistics */
        statistics.registerRequestFromIp(HttpServerStatistics.getIpFromChannel(ctx.channel()), LocalDateTime.now());
        if (request != null) {
            ci.addUri(request.getUri());
        }
    }

    private void writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
        // Decide whether to close the connection or not.
        boolean keepAlive = HttpHeaders.isKeepAlive(request);
        // Build the response object.
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, currentObj.getDecoderResult().isSuccess()? HttpResponseStatus.OK : HttpResponseStatus.BAD_REQUEST,
                Unpooled.copiedBuffer(responseBuffer.toString(), CharsetUtil.UTF_8));

        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=utf-8");

        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
            // Add keep alive header as per:
            // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }

        // Write the response.
        ctx.write(response);

        if (!keepAlive) {
            // If keep-alive is off, close the connection once the content is fully written.
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        } else {
            ctx.flush();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            this.request = (HttpRequest) msg;

            if (HttpHeaders.is100ContinueExpected(request)) {
                send100Continue(ctx);
            }

            // call method based on URI
            new UrlMapper(ctx).callAppropriateMethod();
        }
    }

    private void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
        ctx.write(response);
    }

    private void send501NotImplemented(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_IMPLEMENTED);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE); // close connection
    }

    private void sendRedirect(ChannelHandlerContext ctx, String destinationUri) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
        response.headers().set(HttpHeaders.Names.LOCATION, "http://" + destinationUri);
        statistics.registerRedirect(destinationUri);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void send404NotFound(ChannelHandlerContext ctx) {
        HtmlCreator htmlCreator = new HtmlCreator();
        htmlCreator.setTitle("Not Found");
        htmlCreator.setH1("404 Not Found");
        htmlCreator.addParagraph("The requested URL " + request.getUri() + " was not found on this server.");

        responseBuffer.setLength(0); // clear StringBuilder
        responseBuffer.append(htmlCreator.getHtml());

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND,
                Unpooled.copiedBuffer(responseBuffer.toString(), CharsetUtil.UTF_8));
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=utf-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void serveHelloPage(ChannelHandlerContext ctx) {
        HtmlCreator htmlCreator = new HtmlCreator();
        htmlCreator.setTitle("Hello");
        htmlCreator.setH1("Hello World!");

        responseBuffer.setLength(0);
        responseBuffer.append(htmlCreator.getHtml());

        ctx.executor().schedule(() -> writeResponse(request, ctx), 10, TimeUnit.SECONDS);
    }

    private void serveStatusPage(ChannelHandlerContext ctx) {
        HtmlCreator htmlCreator = new HtmlCreator();
        htmlCreator.setTitle("Statistics");
        htmlCreator.setH1("Statistics");
        htmlCreator.openParagraph().addBold("Total requests: ").addText("" + statistics.getNumberOfRequests()).closeParagraph();
        htmlCreator.openParagraph().addBold("Unique requests: ").addText("" + statistics.getNumberOfUniqueRequests()).closeParagraph();
        htmlCreator.openParagraph().addBold("Open connections: ").addText("" + statistics.getConnectionCount()).closeParagraph();

        htmlCreator.addHorizontalLine();
        htmlCreator.addH2("Requests");
        if (statistics.getIpRequestsAsStrings().size() == 0) {
            htmlCreator.addParagraph("No completed requests.");
        } else {
            List<String> requestsTableHeaders = Arrays.asList("IP", "Requests", "Date and time of last request");
            htmlCreator.addTableWithHeaders(requestsTableHeaders);
            statistics.getIpRequestsAsStrings().forEach(htmlCreator::addRowToTable);
            htmlCreator.endTable();
        }

        htmlCreator.addHorizontalLine();
        htmlCreator.addH2("Redirects");
        if (statistics.getRedirectsAsStrings().size() == 0) {
            htmlCreator.addParagraph("No redirects.");
        } else {
            List<String> redirectsTableHeaders = Arrays.asList("Destination URL", "Number of redirects");
            htmlCreator.addTableWithHeaders(redirectsTableHeaders);
            statistics.getRedirectsAsStrings().forEach(htmlCreator::addRowToTable);
            htmlCreator.endTable();
        }

        htmlCreator.addHorizontalLine();
        htmlCreator.addH2("Connections");
        List<String> connectionsTableHeaders = Arrays.asList("IP", "URIs", "Established", "Closed",
                "Sent (bytes)", "Received (bytes)", "Speed (bytes/sec)");
        htmlCreator.addTableWithHeaders(connectionsTableHeaders);
        statistics.getConnectionsAsStrings().forEach(htmlCreator::addRowToTable);
        htmlCreator.endTable();

        htmlCreator.openStyle().centerHeadings().styleTables().closeStyle();

        responseBuffer.setLength(0);
        responseBuffer.append(htmlCreator.getHtml());

        writeResponse(request, ctx);
    }

}
