package com.company.nio.youtube_slisenko.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

// клиент для запроса на сервер server/netty/NettyServer
public class Client {

    public static void main(String[] args) throws IOException {
        String serverHost = "localhost";
        int serverPort = 45002;
        String message = "Hello there";

        try (Socket socket = new Socket(serverHost, serverPort)) {
            // Отправка сообщения серверу
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            System.out.println(System.currentTimeMillis());
            writer.println(message + ", client time=" + new Date().getTime());   // блокирующий вызов,
            writer.flush();
            System.out.println(System.currentTimeMillis());
            log("send > " + message);

            // Получение ответа от сервера
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println(System.currentTimeMillis());
            String response = reader.readLine();                                 // блокирующий вызов
            System.out.println(System.currentTimeMillis());
            log("received < " + response);
        }
    }

    private static void log(String message) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + message);
    }
}