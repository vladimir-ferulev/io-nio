package com.company.nio.netty.httpserver2.server;

import com.company.nio.netty.httpserver2.Http2Util;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;

// сервер с поддержкой HTTP2 и SSL/TSL. Запросы к этому серверу выполняются по HTTPS
// подробно его не разбирал
public class Http2Server {

    private final int port;

    public Http2Server(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        SslContext sslCtx = Http2Util.createSSLContext(true);

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);     // максимальное количество подключений в очереди. Все, что свыше, будут отброшены
            serverBootstrap.group(group)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(sslCtx.newHandler(ch.alloc()));        // SslHandler. Поддержка SSL/TSL. Обработчик для исходящих данных
                        pipeline.addLast(Http2Util.getServerAPNHandler());      // Настраивает pipeline в зависимости от результата согласования SslHandler. Обработчик для входящих данных
                    }
                });

            Channel channel = serverBootstrap.bind(port).sync().channel();

            System.out.println("HTTP/2 Server is listening on https://127.0.0.1:" + port + '/');

            channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

}
