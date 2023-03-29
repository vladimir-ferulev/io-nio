package com.company.nio.youtube_slisenko.server.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// сервер, написанный на чистом Nio. Может обслуживать параллельно много соединений не блокируясь и используя ограниченное
// количество потоков. В данном случае только один поток - main
public class NIOServer {

    private static final Map<SocketChannel, ByteBuffer> sockets = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        // в отличие от IO, где сокеты, тут каналы. ServerSocketChannel - аналог ServerSocket в IO
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.socket().bind(new InetSocketAddress(45001));

        // установка неблокирующего режима - это необходимое условие для использования селектора для канала
        serverChannel.configureBlocking(false);

        // через селектор получаем уведомления. Он распознает что произошло и сохраняет событие в коллекции
        Selector selector = Selector.open();
        // нужно зарегистрировать селектор каналом, чтобы селектор знал о событиях с каналом и мог сохранить событие этого типа
        // селектор будет фиксировать события, когда с нашим сервером клиенты захотят установить соединение
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            // Сначала в методе select поток блокируется, но если произойдет зарегистрированное событие, то поток разблокируется
            // Но есть и другие варианты, чтобы можно было не блокировать поток и сделать им что-то другое
            // селектор отслеживает все каналы, в которых зарегистрировали этот селектор на разные события
            selector.select();
            // после того как что-то произошло, поток разблокируется. Проверяем у селектора сохраненные события
            for (SelectionKey key : selector.selectedKeys()) {
                if (key.isValid()) {
                    try {
                        if (key.isAcceptable()) {       // если событие - Accept, то значит был принят новый клиент
                            // подключаем нового клиента и создаем канал, через который сервер и клиент будут общаться
                            SocketChannel socketChannel = serverChannel.accept();          // не блокируется. Не может вернуть null, так как есть событие. Гарантировано клиент хочет подключиться
                            socketChannel.configureBlocking(false);                        // устанавливаем неблокирующий режим, чтобы можно было использовать селектор для этого канала
                            ByteBuffer buffer = ByteBuffer.allocate(1000);                 // выделяем буфер для клиентского канала, изначально он настроен на запись в буфер, то есть указатель position в начале, указатель limit в конце
                            sockets.put(socketChannel, buffer);
                            socketChannel.register(selector, SelectionKey.OP_READ);        // селектор будет отслеживать момент, когда можно будет из канала что-то прочитать (когда в буфер ОС есть данные)
                        } else if (key.isReadable()) {                                     // если из канала что-то можно прочитать (событие будет когда из сети пришли данные и хранятся в буфере ОС)
                            SocketChannel socketChannel = (SocketChannel) key.channel();   // из события достаем канал, который стриггерил это событие с типом OP_READ
                            ByteBuffer buffer = sockets.get(socketChannel);
                            int bytesRead = socketChannel.read(buffer);                    // неблокирующий вызов read и запись данных из буфера ОС в буфер Java приложения для канала

                            // если соединение завершено
                            if (bytesRead == -1) {
                                sockets.remove(socketChannel);
                                socketChannel.close();
                            }

                            // определяем конец сообщения. В нашем случае концом сообщения будет перенос строки
                            // если переноса строки не будет, то будем читать данные до тех пор, пока он не встретится. Возвращаемся в начало цикла while
                            // и если в буфере ОС еще остались данные для чтения, то снова попадаем в эту ветку
                            if (bytesRead > 0 && buffer.get(buffer.position() - 1) == '\n') {
                                // переключаем селектор с отслеживания возможности чтения из канала на отслеживание возможности записи в канал
                                socketChannel.register(selector, SelectionKey.OP_WRITE);
                            }
                        } else if (key.isWritable()) {   // если есть условия что-то записать в канал (условия описаны в тексте перед блоком с кодом в пункте 2)
                            SocketChannel socketChannel = (SocketChannel) key.channel();    // из события достаем канал, который стриггерил событие
                            ByteBuffer buffer = sockets.get(socketChannel);                 // достаем буфер, который храним в своей мапе
                            // меняем режим буфера на чтение из буфера. Изначально было на запись в буфер
                            buffer.flip();
                            // читаем данные из буфера, которые пришли по каналу от клиента и которые мы ранее прочитали из сети
                            String clientMessage = new String(buffer.array(), buffer.position(), buffer.limit());
                            // построение ответа сервера клиенту
                            String response = clientMessage.replace("\r\n", "") + ", server time=" + System.currentTimeMillis() + "\r\n";

                            buffer.clear();                                         // подготавливаем буфер для записи данных в него
                            buffer.put(ByteBuffer.wrap(response.getBytes()));       // добавляем сообщение в буфер, которое хотим отправить клиенту
                            buffer.flip();                                          // подготавливаем буфер для чтения данных из буфера. Чтение из буфера будет происходить во время отправки данных в сеть

                            // отправляем данные из буфера в сеть. Могут не все данные сразу записаться. Например, если получатель не может столько принять в свой буфер ОС.
                            // Если в буфере данные еще остались, то снова отработает
                            // событие OP_WRITE, снова в цикле попадем в эту ветку. И так до тех пор, пока не отправим все данные
                            int bytesWritten = socketChannel.write(buffer);
                            if (!buffer.hasRemaining()) {
                                buffer.compact();
                                socketChannel.register(selector, SelectionKey.OP_READ);
                            }
                        }
                    } catch (IOException ignore) {}
                }
            }

            selector.selectedKeys().clear();
        }
    }
}