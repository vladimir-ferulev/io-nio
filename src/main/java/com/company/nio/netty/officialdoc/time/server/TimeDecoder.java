package com.company.nio.netty.officialdoc.time.server;

import com.company.nio.netty.officialdoc.time.entity.UnixTime;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

// три способа реализации декодирования полученного из сети времени

    // ========================= 1) подсчитывать количество байтов вручную ===========================
    // здесь используется готовый обработчик Netty - ByteToMessageDecoder
    // передаем ByteBuf дальше, только когда сам получили ByteBuf с достаточным количеством данных
//public class TimeDecoder extends ByteToMessageDecoder {
//    @Override
//    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
//        if (in.readableBytes() < 4) {
//            return;
//        }
//
//        out.add(in.readBytes(4));
//    }
//}

    // ========================= 2) использовать ReplayingDecoder ===========================
    // ReplayingDecoder - decode будет вызван только когда все 4 байта будут доступны

//public class TimeDecoder extends ReplayingDecoder<Void> {
//    @Override
//    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
//        out.add(in.readBytes(4));
//    }
//}

// ========================= 3) то же самое, что и 1), но используется POJO ===========================
public class TimeDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 4) {
            return;
        }

        out.add(new UnixTime(in.readUnsignedInt()));
    }
}

