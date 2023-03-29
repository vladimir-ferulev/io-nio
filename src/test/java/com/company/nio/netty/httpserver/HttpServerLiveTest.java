package com.company.nio.netty.httpserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.util.CharsetUtil;

// Тесты для com.company.nio.netty.httpserver.HttpServer
// Перед запуском тестов нужно запустить сервер HttpServer с правильным портом
public class HttpServerLiveTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 8082;
    private Channel channel;
    private EventLoopGroup group = new NioEventLoopGroup();
    ResponseAggregator response = new ResponseAggregator();

    // Для каждого теста предварительно настраивается клиент и устанавливается соединение с сервером.
    // И в итоге получаем канал для передачи данных между клиентом и сервером и работаем в тестах с ним.
    @Before
    public void setup() throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new HttpClientCodec());            // объединяет HttpRequestEncoder and HttpResponseDecoder
                        pipeline.addLast(new HttpContentDecompressor());    // распаковывает HttpMessage и HttpContent, сжатые в кодировке gzip или deflate
                        pipeline.addLast(new SimpleChannelInboundHandler<HttpObject>() {    // для одного HTTP запроса вызывается два раза: сам HTTP запрос и тело запроса, если есть
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
                                response = prepareResponse(ctx, msg, response);
                            }
                        });
                    }
                });

        channel = bootstrap.connect(SERVER_HOST, SERVER_PORT)
                .sync()
                .channel();
    }

    @Test
    public void whenPostSent_thenContentReceivedInUppercase() throws Exception {
        String body = "Hello World!";

        DefaultFullHttpRequest request = createRequest(body);

        channel.writeAndFlush(request); // можем передать DefaultFullHttpRequest, так как далее данные принимает обработчик HttpClientCodec.
                                        // HttpClientCodec типизирован интерфейсом HttpObject, который реализует DefaultFullHttpRequest
        Thread.sleep(200);

        assertEquals(200, response.getStatus());
        assertEquals("HTTP/1.1", response.getVersion());

        assertTrue(response.getContent().contains(body.toUpperCase()));
    }

    @Test
    public void whenGetSent_thenResponseOK() throws Exception {
        DefaultFullHttpRequest request = createRequest(null);

        channel.writeAndFlush(request);
        Thread.sleep(200);

        assertEquals(200, response.getStatus());
        assertEquals("HTTP/1.1", response.getVersion());
    }

    // после каждого теста закрываем канал и event loop
    @After
    public void cleanup() throws InterruptedException {
        channel.closeFuture().sync();
        group.shutdownGracefully();
    }

    private static DefaultFullHttpRequest createRequest(CharSequence body) throws Exception {
        DefaultFullHttpRequest request;
        if (body != null) {
            request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");
            request.content().writeBytes(body.toString().getBytes(CharsetUtil.UTF_8));
            request.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
            request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        } else {
            request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/", Unpooled.EMPTY_BUFFER);
            request.headers().set(HttpHeaderNames.COOKIE, ClientCookieEncoder.STRICT.encode(new DefaultCookie("my-cookie", "foo")));
        }

        request.headers().set(HttpHeaderNames.HOST, SERVER_HOST);
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

        return request;
    }

    private static ResponseAggregator prepareResponse(ChannelHandlerContext ctx, HttpObject msg, ResponseAggregator responseAgg) {
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;

            responseAgg.setStatus(response.status().code());
            responseAgg.setVersion(response.protocolVersion().text());
            responseAgg.setChunked(HttpUtil.isTransferEncodingChunked(response));
            if (!response.headers().isEmpty()) {
                Map<String, String> headers = new HashMap<>();
                for (CharSequence name : response.headers().names()) {
                    for (CharSequence value : response.headers().getAll(name)) {
                        headers.put(name.toString(), value.toString());
                    }
                }
                responseAgg.setHeaders(headers);
            }
        }
        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;
            String responseData = content.content().toString(CharsetUtil.UTF_8);

            if (content instanceof LastHttpContent) {
                responseAgg.setContent(responseData + "} End Of Content");
                ctx.close();
            }
        }
        return responseAgg;
    }
}
