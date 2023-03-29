package com.company.nio;

import com.company.nio.netty.httpserver.HttpServer;
import com.company.nio.netty.httpserver2.server.Http2Server;

public class NioMain {
    public static void main(String[] args) throws Exception {
        // раскоментировать только что-то одно

        // чтение и запись данных
//        NioReadWriteFile.readFile();
//        NioReadWriteFile.writeToFile();
//        NioReadWriteFile.readFileMappedChannel();


        // серверы
//        new HttpServer(8082).run();
        new Http2Server(8082).run();
//        new OfficialDocServer(8082).run();
    }
}


