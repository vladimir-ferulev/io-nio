package com.company.nio.youtube_slisenko.client;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// тестирование различных серверов на количество одновременных соединений и на выделение потоков на сервере
public class LoadTestingClient {

    public static void main(String[] args) throws InterruptedException, IOException {
//        int serverPort = 45000;       // IO server
        int serverPort = 45001;       // NIO server
//        int serverPort = 45002;       // Netty server

        List<Socket> sockets = new ArrayList<>();
        System.out.println("Opening sockets");
        for (int i = 0; i < 1000; i++) {
            try {
                System.out.println(i);
                sockets.add(new Socket("localhost", serverPort));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.print("Print any string to exit");
        new Scanner(System.in).next();

        // Closing connections
        System.out.print("Closing connections");
        for (Socket socket : sockets) {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("error closing socket " + e.getMessage());
            }
        }
    }
}