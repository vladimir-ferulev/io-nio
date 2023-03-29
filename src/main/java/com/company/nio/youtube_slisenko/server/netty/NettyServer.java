package com.company.nio.youtube_slisenko.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class NettyServer {

    public static void main(String[] args) throws InterruptedException {
        // Мы должны привязать event loop к каждому каналу. Все операции ввода/вывода для отдельного канала всегда выполняются в одном потоке.
        // Один event loop разделяется между множеством каналов

        // Используются две отдельные группы. Если у нас высокая нагрузка и объем данных и только 1 группа event loop,
        // она становится слишком занятой, чтобы принимать новые подключения, и они отбиваются по тайм-ауту.
        // Вот почему есть отдельный event loop для приема новых подключений.
        // Event loop разделяется между каналами. Вот причина, по которой мы НЕ ДОЛЖНЫ его блокировать.
        EventLoopGroup bossGroup = new NioEventLoopGroup();         // Для приема новых подключений
        EventLoopGroup workerGroup = new NioEventLoopGroup();       // Для обработки ввода-вывода на существующих соединениях

        // У Netty есть pipeline каналов — список обработчиков каналов.
        // Pipeline содержит входящие и исходящие обработчики.
        // Обработчики НЕ ДОЛЖНЫ блокировать потоки ввода-вывода!
        // Если нам нужно сделать блокирующую операцию, то делаем это в другом потоке.
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)      // У Netty много разных реализаций передачи данных, один из них NIO

                // Для каждого канала Netty настраивает pipeline.
                // Pipeline - это упорядоченный список обработчиков.
                // Обработчик отвечает за обработку сообщения как сервлеты или фильтры
                // Обработчик может обрабатывать событие и при необходимости передавать его следующему обработчику.
                // Каждый обработчик может отправлять сообщения обратно, то есть вернуть ответ, а не передать следующему обработчику

                // Мы можем добавить обработчики кодирования/декодирования. Например, обработчик HTTP или Protobuf.
                // Что, если нам нужно сделать кодирование/декодирование, а не блокировать event loop?
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // DelimiterBasedFrameDecoder - разбивает полученный ByteBuf с помощью разделителя на отдельные ByteBuf
                        // StringDecoder - преобразует ByteBuf в String
                        // StringEncoder - преобразует String в ByteBuf (обработчик сообщения при отправке ответа клиенту)
                        // EchoServerHandler - обработчик для бизнес-логики (единственный здесь кастомный обработчик)
                        ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1000, Delimiters.lineDelimiter()));
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(new StringEncoder());
                        ch.pipeline().addLast(new EchoServerHandler());
                    }
                });

            ChannelFuture channelFuture = serverBootstrap.bind(45002).sync();
            System.out.println("Starting nio server at " + channelFuture.channel().localAddress());

            // Ждать пока server socket не закроется
            channelFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}