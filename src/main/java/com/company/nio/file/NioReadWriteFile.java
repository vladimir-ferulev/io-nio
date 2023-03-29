package com.company.nio.file;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

public class NioReadWriteFile {
    private final static String filePath = "testFiles/fileOut2";

    public static void readFile() throws IOException {
        // доступ к файлу в одну сторону - чтение
        // создаем канал для передачи данных из файла
        FileChannel channel = new RandomAccessFile(filePath, "r").getChannel();

        // есть другие способы создания канала
//        FileChannel channel = FileChannel.open(Paths.get(filePath), StandardOpenOption.READ);
//        FileChannel channel = (FileChannel) Files.newByteChannel(Paths.get(filePath), StandardOpenOption.READ);
//        FileChannel channel = new FileInputStream(filePath).getChannel();

        // для канала создаем буфер. Он необходим для работы канала. Он является частью канала
        // изначально буфер подготовлен для записи в буфер, то есть указатель position вначале, limit в конце
        ByteBuffer buffer = ByteBuffer.allocate(10);

        int bytesRead = channel.read(buffer);              // читаем из файла и записываем данные в буфер. Нативный метод read
        while (bytesRead != -1) {                          // как и в IO "-1" является признаком окончания файла
            buffer.flip();                                 // указатель position встает в начало и тогда при получении данных через get
                                                           // мы получаем байты с самого начала буфера. limit устанавливается в позицию последнего
                                                           // прочитанного байта из канала, чтобы при чтении знали до какого байта читать

            while (buffer.hasRemaining()) {                // если в буфере есть непрочитанные данные. По факту, если position еще не дошел до limit
                System.out.print((char) buffer.get());     // читаем эти данные. Данные при этом не удаляются, просто передвигается курсор position
            }

            buffer.clear();                                // указатель position встает в начало, чтобы запись в буфер начиналась сначала
                                                           // указатель limit устанавливается в конец, чтобы была возможность записать данные в полностью весь буфер
            bytesRead = channel.read(buffer);              // читаем из файла и записываем данные в буфер
        }

        // На самом деле никаких режимов записи и чтения у буфера нет.
        // Методами flip, clear мы просто перемещаем указатели position и limit в буфере так, чтобы была возможность записать или прочитать данные.
        // Метод flip устанавливает position на начало буфера, а limit на последний записанный байт.
        // Нам необязательно читать данные в таком случае, можем и писать. В общем делать что угодно.
        // Но flip подготавливает именно для чтения из буфера.
    }

    public static void writeToFile() throws IOException {
        // доступ к файлу в обе стороны - чтение/запись
        // создаем канал для передачи данных в файл
        FileChannel channel = new RandomAccessFile(filePath, "rw").getChannel();

        channel.position(channel.size());                   // если хотим дописать в файл, а не переписать его

        // для канала создаем буфер. Он необходим для работы канала. Он является частью канала
        // изначально буфер подготовлен для записи в буфер, то есть указатель position вначале, limit в конце
        ByteBuffer buffer = ByteBuffer.allocate(50);      // создаем буфер, в который будем добавлять данные, чтобы затем отправить в файл
        buffer.put("Hello".getBytes());                           // добавляем данные в буфер. Но в файл пока что ничего не отсылается
        buffer.flip();                                      // устанавливает limit на position, а position в начало. Это подготавливает буфер
                                                            // к тому, чтобы из него читались данные и писались в файл
        channel.write(buffer);                              // отправляем данные из буфера в файл. Нативный метод write
                                                            // во время передачи данных из буфера в файл position дойдет до limit
    }


    // чтение файла напрямую из памяти. Файл мапится в специальном буфере, общем для Java приложения и для ОС
    public static void readFileMappedChannel() throws IOException {
        try (FileChannel channel = (FileChannel) Files.newByteChannel(Paths.get(filePath))) {
            long size = channel.size();

            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, size);

            for (int i = 0; i < size; i++) {
                System.out.print((char) buffer.get());
            }
        }
    }
}

