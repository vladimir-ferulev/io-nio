package com.company.nio.netty.officialdoc.time.server;

import com.company.nio.netty.officialdoc.time.entity.UnixTime;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

// два способа реализации кодирования в байты отправленного времени в сеть
// ==================== 1) написание логики кодирования данных и передачи в сеть вручную ==================
//public class TimeEncoder extends ChannelOutboundHandlerAdapter {
//    @Override
//    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
//        UnixTime m = (UnixTime) msg;
//        ByteBuf encoded = ctx.alloc().buffer(4);
//        encoded.writeInt((int) m.value());
//        ctx.write(encoded, promise);
//    }
//}

// =================== 2) использовать готовый класс из библиотеки Netty - MessageToByteEncoder ==============
public class TimeEncoder extends MessageToByteEncoder<UnixTime> {
    @Override
    protected void encode(ChannelHandlerContext ctx, UnixTime msg, ByteBuf out) {
        out.writeInt((int) msg.value());
    }
}