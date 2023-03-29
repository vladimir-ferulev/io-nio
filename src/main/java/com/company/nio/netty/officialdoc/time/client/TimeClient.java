package com.company.nio.netty.officialdoc.time.client;

import com.company.nio.netty.officialdoc.time.server.TimeDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

// клиент для Time Server. После старта сразу установит соединение с сервером и получит от него время
public class TimeClient {
    public static void main(String[] args) throws Exception {
        String serverHost = "localhost";
        int serverPort = 8082;
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new TimeDecoder());
                    pipeline.addLast(new TimeClientHandler());
                }
            });

            // Установление соединения клиента с сервером.
            // В методе sync() будет ожидание до тех пор, пока соединение не установится
            ChannelFuture channelFuture = bootstrap.connect(serverHost, serverPort).sync();

            // Ждем пока соединение не будет закрыто
            channelFuture.channel()
                    .closeFuture()  // Возвращает ChannelFuture, который будет уведомлен, когда этот канал будет закрыт.
                    .sync();        // Ожидание закрытия канала. Как только канал клиент-сервер закроется, то это метод будет выполнен
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}