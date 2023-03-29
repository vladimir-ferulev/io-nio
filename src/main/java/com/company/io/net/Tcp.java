package com.company.io.net;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Tcp {
    public static int TCP_PORT = 8081;
    public static String HTTP_RESPONSE = "HTTPS/1.1 200 OK\r\nContent-Type: text/html\r\n\r\nHello World;\n";

    // Получение и отображение потока байтов. Можно установить соединение только одному клиенту
    public static void runPrimitiveTcpServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(TCP_PORT);     // выполняются нативные методы bind и listen
        Socket socket = serverSocket.accept();                      // выполняется нативный метод accept. Ждем соединения с клиентом

        InputStream is = socket.getInputStream();                   // возвращается SocketInputStream

        int i;
        while ((i = is.read()) != -1) {                             // читаем из буфера ОС, пока не получим признак завершения соединения "-1". Ждем если данных в буфере нет.
            System.out.print(i);
        }

        is.close();
        socket.close();
        serverSocket.close();
    }

    public static void runTcpServerAndClient() throws IOException {
        startTcpClientWithDelay();
        startTcpServer();
    }

    public static void startTcpServer() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT);        // выполняются нативные методы bind и listen
             Socket socket = serverSocket.accept();                         // выполняется нативный метод accept
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8));
             OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream(), UTF_8)) {

            String line;
            while ((line = reader.readLine()) != null) {             // если line == null, то клиент завершил отправку сообщений
                if ("end".equals(line)) break;                       // завершить получение данных со стороны сервера, если клиент отправил "end"
                System.out.println(line);
            }

            String outMessage = "Server response";
            writer.write(outMessage);
            writer.flush();
        }
    }

    public static void startTcpClient() throws IOException, InterruptedException {
        try (Socket socket = new Socket("localhost", TCP_PORT);           // устанавливается соединение с сервером
             OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream(), UTF_8);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8))) {

            writer.write("Client request");
            writer.write("\nend\n");
            writer.flush();

            reader.lines().forEach(System.out::println);        // вывести ответ сервера
        }
    }


    private static void startTcpClientWithDelay() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                startTcpClient();
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
