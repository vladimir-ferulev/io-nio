package com.company.io.file;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class IoReadWriteFile {
    private final static String fileName = "testFiles/fileOut";

    // вызывает нативный метод записи при каждом вызове write
    public static void writeToFileSimple() {
        try (FileOutputStream fos = new FileOutputStream(fileName, true)) {
            fos.write(65);
            fos.write('\n');
            fos.write("String".getBytes());
            fos.write("Hello world".getBytes(), 5, 6);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // при вызовах write сначала заполняет буфер в Java приложении и когда заполнится буфер
    // или вызовется flush() или закроется стрим, то вызовется нативный метод записи данных в файл
    public static void writeToFileBuffered() {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fileName, true))) {
            bos.write(65);
            bos.write('\n');
            bos.flush();
            bos.write("String".getBytes());
            bos.write("Hello world".getBytes(), 5, 6);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //  в отличие от FileOutputStream, который записывает только байты, позволяет записывать тип String
    public static void writeToFileWriter() {
        try (FileWriter fw = new FileWriter(fileName, true)) {
            fw.write("A");
            fw.write('\n');
            fw.write("String");
            fw.write("Hello world", 5, 6);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // каждый вызов read() вызывает нативный метод чтения данных из файлов и возвращает байты
    public static void readFileSimple() {
        try (FileInputStream fis = new FileInputStream(fileName)) {
            int byt = fis.read();
            System.out.println(byt);

            byte[] bytes = new byte[10];
            fis.read(bytes);
            System.out.println(Arrays.toString(bytes));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Даже если сделать вызов чтения только одного байта, то из файла будет прочитан объем данных, равный буферу
    // и уже из него будем читать по одному байту или другими объемами данных внутри Java приложения пока не прочитаем весь массив.
    // После прочтения всего массива снова будет вызван нативный метод чтения данных из файла
    public static void readFileBuffered() {
        try (BufferedInputStream bos = new BufferedInputStream(Files.newInputStream(Paths.get(fileName)))) {
            int byt = bos.read();
            System.out.println(byt);

            byte[] bytes = new byte[10];
            bos.read(bytes);
            System.out.println(Arrays.toString(bytes));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // читаем сразу символы
    public static void readFileReader() {
        try (FileReader fr = new FileReader(fileName)) {
            int symbolCode = fr.read();
            System.out.println((char) symbolCode);

            char[] symbols = new char[10];
            fr.read(symbols);
            System.out.println(Arrays.toString(symbols));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
