package com.company.nio.netty.officialdoc.discard;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

// Обработчик канала на стороне сервера
public class DiscardServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // отбросить полученные данные без каких-либо дополнительных действий
        ((ByteBuf) msg).release(); // уменьшает счетчик ссылок на 1 и освобождает этот объект, если счетчик ссылок достигает 0.
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // закрыть соединение при возникновении исключения
        cause.printStackTrace();
        ctx.close();
    }
}