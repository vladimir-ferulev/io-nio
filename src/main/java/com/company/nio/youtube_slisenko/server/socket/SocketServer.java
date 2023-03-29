package com.company.nio.youtube_slisenko.server.socket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// примеры простых серверов на IO, которые не подходят для очень большого количества соединений в одно время
public class SocketServer {

    public static void main(String[] args) throws IOException {
//        runServerWithOneThread();
//        runServerWithManyThreads();
        runServerWithThreadPool();
    }

    // 1) Так как один поток, то пока не завершится работа с одним соединением, следующее не будет установлено
    public static void runServerWithOneThread() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(45000)) {
            System.out.println("Server start at port 45000. Listening for client connections...");
            while (true) {
                final Socket socket = serverSocket.accept();
                handle(socket);
            }
        }
    }

    // 2) Будет создано столько потоков, сколько будет соединений.
    // Ограничения одновременного количества потоков нет, что может привести к заполнению памяти
    public static void runServerWithManyThreads() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(45000)) {
            System.out.println("Server start at port 45000. Listening for client connections...");
            while (true) {
                final Socket socket = serverSocket.accept();
                new Thread(() -> {
                    try {
                        handle(socket);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        }
    }

    // 3) Используем пул потоков, что дает возможность установить количество потоков и не заполнить память. Остальные
    // соединения будут ждать очереди. Если соединений будет много, то они будут ждать освобождения потоков в пуле.
    public static void runServerWithThreadPool() throws IOException {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try (ServerSocket serverSocket = new ServerSocket(45000)) {
            System.out.println("Server start at port 45000. Listening for client connections...");
            while (true) {
                final Socket socket = serverSocket.accept();
                pool.submit(() -> {
                    try {
                        handle(socket);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } finally {
            pool.shutdown();
        }
    }

    private static void handle(Socket socket) throws IOException {
        try (InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream()) {

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            PrintWriter writer = new PrintWriter(out);

            String line = reader.readLine();
            writer.write(line + ", " + System.currentTimeMillis());
            writer.flush();
        }
    }
}