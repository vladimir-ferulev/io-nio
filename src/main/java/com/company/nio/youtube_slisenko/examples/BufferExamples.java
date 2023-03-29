package com.company.nio.youtube_slisenko.examples;

import java.nio.ByteBuffer;

// примеры использования буфера в NIO
public class BufferExamples {

    public static void main(String[] args) {
        // после создания буфера указатели: position = 0 (min); limit = 50 (max); capacity = 50 (max)
        ByteBuffer buf = ByteBuffer.allocate(50);

        // capacity всегда будет 50, поэтому дальше нет смысла это указывать

        // после добавления "hello": position = 5; limit = 50
        buf.put("hello".getBytes());
        printBufferDetails("1, +hello", buf);

        // после добавления "world": position = 10; limit = 50
        buf.put("world".getBytes());
        printBufferDetails("2, +world", buf);

        // после вызова clear(): position = 0; limit = 50
        // данные в буфере при этом не очищаются, только переставляются указатели position и limit
        // limit не поменялся, т.к. clear его устанавливает в конец, но он и так был в конце
        buf.clear();
        printBufferDetails("3, clear()", buf);

        // после добавления "AAA": position = 3; limit = 50
        buf.put("AAA".getBytes());
        printBufferDetails("4, +AAA", buf);

        // читаем один символ: position = 4; limit = 50
        // в буфере под индексом 3 была буква l из слова hello
        System.out.println((char) buf.get());
        printBufferDetails("4, get()", buf);

        // после вызова flip(): position = 0, limit = 4
        // методом flip подготавливаем буфер для чтения ранее записанных данных,
        // но по факту просто устанавливаем limit на место position, а position в начало
        // буферу все равно по какой причине position до метода flip() был продвинут вперед - чтением или записью в буфер
        buf.flip();
        printBufferDetails("5, flip()", buf);

        // после вызова clear(): position = 0; limit = 50
        // данные в буфере при этом не очищаются, только переставляются указатели position и limit
        buf.clear();
        printBufferDetails("5, clear()", buf);

        // после добавления "123": position = 3; limit = 50
        // Методом mark() делаем отметку текущего положение position = 3,
        // чтобы потом можно было выполнить reset() и вернуть указатель position к индексу = 3
        // после добавления "456": position = 6; limit = 50
        buf.put("123".getBytes());
        buf.mark();
        buf.put("456".getBytes());
        printBufferDetails("6, +123 mark() +456", buf);

        // после вызова reset(): position = 3; limit = 50
        buf.reset();
        printBufferDetails("7, reset()", buf);

        // после вызова rewind(): position = 0; limit = 50
        // rewind только устанавливает position в 0, limit не изменяет. Если бы например limit был бы = 10, то
        // он так и остался бы 10. rewind позволяет перечитать снова уже прочитанные данные.
        // отметка mark будет очищена. Отметка очищается и другими методами - flip, clear
        buf.rewind();
        printBufferDetails("8, rewind()", buf);
    }

    public static void printBufferDetails(String comment, ByteBuffer buf) {
        System.out.println(comment + " [" + new String(buf.array()) + "]" + " Position=" + buf.position() +
                " Limit=" + buf.limit() + " Capacity=" + buf.capacity() +
                " Remaining=" + buf.remaining() +
                " ArrayOffset=" + buf.arrayOffset() + " IsReadOnly=" + buf.isReadOnly());
    }
}