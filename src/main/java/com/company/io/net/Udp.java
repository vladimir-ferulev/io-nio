package com.company.io.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static java.nio.charset.StandardCharsets.*;

public class Udp {
    public static int SERVER_UDP_PORT = 8082;

    public static void runEchoUdpServerAndClient() throws IOException {
        startUdpClientWithDelay();
        startUpdServer(false);
    }

    // Получаем от клиента пакет и тут же отправляем его обратно без установки пакету обратного адреса клиента - ip и port.
    // Пакет успешно отправляется назад. Когда он приходит из сети, то ему автоматически меняется адрес назначения на обратный еще на уровне ОС
    public static void runEchoUdpServerAndClientResendSamePacket() throws IOException {
        startUdpClientWithDelay();
        startUpdServer(true);
    }

    public static void startUpdServer(boolean resendSamePacket) throws IOException {
        try (DatagramSocket serverSocket = new DatagramSocket(SERVER_UDP_PORT)) {        // выполняется нативный метод bind
            byte[] receiveBuf = new byte[1024];
            byte[] sendBuf;

            DatagramPacket inputPacket = new DatagramPacket(receiveBuf, receiveBuf.length);
            serverSocket.receive(inputPacket);                              // блокировка и ожидание данных от клиента

            String receivedData = new String(inputPacket.getData(), 0, inputPacket.getLength(), UTF_8);
            System.out.println("Sent from the client: " + receivedData);

            sendBuf = receivedData.toUpperCase().getBytes();                // подготавливаем данные для обратной отправки клиенту
            InetAddress clientAddress = inputPacket.getAddress();
            int clientPort = inputPacket.getPort();
            
            if (resendSamePacket) {
                inputPacket.setData(sendBuf, 0, sendBuf.length);
                serverSocket.send(inputPacket);                             // установка адреса клиента в пакете не нужна, адрес автоматически проставляется еще в ОС
            } else {
                DatagramPacket outputPacket = new DatagramPacket(sendBuf, sendBuf.length, clientAddress, clientPort);
                serverSocket.send(outputPacket);
            }
        }
    }

    public static void startUpdClient() throws IOException {
        try (DatagramSocket clientSocket = new DatagramSocket()) {      // выполняется нативный метод bind
            InetAddress serverAddress = InetAddress.getByName("localhost");

            byte[] sendBuf = "Hello from UDP client".getBytes(UTF_8);
            byte[] receiveBuf = new byte[1024];

            DatagramPacket outputPacket = new DatagramPacket(sendBuf, sendBuf.length, serverAddress, SERVER_UDP_PORT);
            clientSocket.send(outputPacket);

            DatagramPacket inputPacket = new DatagramPacket(receiveBuf, receiveBuf.length);
            clientSocket.receive(inputPacket);                          // блокировка и ожидание ответа от сервера

            String receivedData = new String(inputPacket.getData(), 0, inputPacket.getLength());
            System.out.println("Sent from the server: " + receivedData);
        }
    }

    private static void startUdpClientWithDelay() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                startUpdClient();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
