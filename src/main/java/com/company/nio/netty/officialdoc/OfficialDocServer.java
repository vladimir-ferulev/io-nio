package com.company.nio.netty.officialdoc;

import com.company.nio.netty.officialdoc.time.server.TimeEncoder;
import com.company.nio.netty.officialdoc.time.server.TimeServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class OfficialDocServer {

    private final int port;

    public OfficialDocServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            // раскомментировать только один из вариантов в зависимости от того, какой сервер хотим запустить
                            // 1) discard server
//                            pipeline.addLast(new DiscardServerHandler());

                            // 2) echo server
//                            pipeline.addLast(new EchoServerHandler());

                            // 3) time server
                            pipeline.addLast(new TimeEncoder());
                            pipeline.addLast(new TimeServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // максимальное количество подключений в очереди. Все, что свыше, будут отброшены
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // биндит порт и начинает принимать входящие подключения от клиентов
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();

            // Ждать до тех пор, пока серверный сокет не закроется
            // В этом примере это не произойдет, но это можно сделать, чтобы корректно завершить работу сервера
            channelFuture.channel()
                    .closeFuture()  // Возвращает ChannelFuture, который будет уведомлен, когда этот канал будет закрыт.
                    .sync();        // Ожидание закрытия канала.
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}