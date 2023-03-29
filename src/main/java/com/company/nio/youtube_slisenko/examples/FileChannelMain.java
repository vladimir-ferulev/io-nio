package com.company.nio.youtube_slisenko.examples;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

// примеры записи в файл и чтения из файла используя NIO
public class FileChannelMain {

    public static void main(String[] args) throws IOException {
        RandomAccessFile file = new RandomAccessFile("target/raf.txt", "rw");
        FileChannel channel = file.getChannel();

        // запись в файл
        ByteBuffer bigBuffer = ByteBuffer.allocate(10);
        bigBuffer.put("hello-nio".getBytes());
        bigBuffer.flip(); // если не выполним flip(), то данные в файл не запишутся

        channel.write(bigBuffer);
        channel.force(true);      // ОС не будет кэшировать изменения файла, а будет сразу изменять его. Работает только, если файл находится локально
        channel.position(0);

        // чтение из файла
        ByteBuffer smallBuffer = ByteBuffer.allocate(5);

        int bytesRead = channel.read(smallBuffer);      // прочитали только часть данных, так как все не поместилось в буфер
        System.out.println("BytesRead=" + bytesRead + " [" + new String(smallBuffer.array()) + "]");
        smallBuffer.clear();        // переместили position в буфере в 0

        bytesRead = channel.read(smallBuffer);          // дочитали данные из файла
        System.out.println("BytesRead=" + bytesRead + " [" + new String(smallBuffer.array()) + "]");
    }
}