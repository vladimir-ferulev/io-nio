package com.company.nio.netty.officialdoc.time.client;

import com.company.nio.netty.officialdoc.time.entity.UnixTime;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

// обработчик для полученных данных от сервера
public class TimeClientHandler extends ChannelInboundHandlerAdapter {

    // ========================== 1) способ без проверки полноты данных ================================
    // простейший вариант, при котором в теории можем получить частичные данные, когда читаем из буфера ОС
    // TimeDecoder не используется
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) {
//        ByteBuf m = (ByteBuf) msg;
//        try {
//            long currentTimeMillis = (m.readUnsignedInt() - 2208988800L) * 1000L;
//            System.out.println(new Date(currentTimeMillis));
//            ctx.close();
//        } finally {
//            m.release();
//        }
//    }


    // ======================= 2) первое решение, чтобы получить полные данные ======================
    // в этом варианте не используется отдельный обработчик TimeDecoder. Вся логика здесь, в текущем обработчике
//    private ByteBuf buf;
//
//    @Override
//    public void handlerAdded(ChannelHandlerContext ctx) {
//        buf = ctx.alloc().buffer(4);
//    }
//
//    @Override
//    public void handlerRemoved(ChannelHandlerContext ctx) {
//        buf.release();
//        buf = null;
//    }
//
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) {
//        ByteBuf m = (ByteBuf) msg;
//        buf.writeBytes(m);
//        m.release();
//
//        if (buf.readableBytes() >= 4) {
//            long currentTimeMillis = (buf.readUnsignedInt() - 2208988800L) * 1000L;
//            System.out.println(new Date(currentTimeMillis));
//            ctx.close();
//        }
//    }
//
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//        cause.printStackTrace();
//        ctx.close();
//    }


    // ================== 3) Используется POJO вместо ByteBuf =====================
    // этот вариант используется совместно с TimeDecoder
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        UnixTime m = (UnixTime) msg;
        System.out.println(m);
        ctx.close();
    }

    // одинаковый для всех вариантов
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
