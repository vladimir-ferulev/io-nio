package com.company.nio.netty.officialdoc.time.server;

import com.company.nio.netty.officialdoc.time.entity.UnixTime;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

// Обработчик канала на стороне сервера
public class TimeServerHandler extends ChannelInboundHandlerAdapter {

    // здесь два варианта 1) работа с полученными данными в буфере, 2) работа с полученными данными в Java объекте

        // ================= 1) ByteBuf ====================

//    @Override
//    public void channelActive(final ChannelHandlerContext ctx) {
//        final ByteBuf time = ctx.alloc().buffer(40);
//        time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));  // 2208988800L - это начало UNIX time (1970 г.), поэтому его нужно приплюсовать
//        time.writeBoolean(true);
//
//        final ChannelFuture channelFuture = ctx.writeAndFlush(time);
//        channelFuture.addListener(new ChannelFutureListener() {
//            @Override
//            public void operationComplete(ChannelFuture future) {
//                assert channelFuture == future;
//                ctx.close();
//            }
//        });
//    }

    // ============= 2) POJO вместо ByteBuf ================

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ChannelFuture channelFuture = ctx.writeAndFlush(new UnixTime());
        channelFuture.addListener(ChannelFutureListener.CLOSE);
    }


    // общий для всех случаев
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}