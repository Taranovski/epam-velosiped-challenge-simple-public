package dev.abarmin.velosiped.task1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class VelosipedTask1Impl implements VelosipedTask1 {

    private volatile ServerSocket serverSocket;
    private volatile boolean serverServesRequests;
    int port;

    @Override
    public void startServer(int port) {
        this.port = port;
        try {
            serverSocket = new ServerSocket(port);
            serverServesRequests = true;

            new Thread(new MyRunnable()).start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stopServer() {
        try {
            //todo graceful shutdown?
            serverServesRequests = false;
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class MyRunnable implements Runnable {
        @Override
        public void run() {
            while (serverServesRequests) {

            try {
                try (
                        Socket socket = serverSocket.accept();

                        InputStream inputStream = socket.getInputStream();
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                        BufferedReader stringReader = new BufferedReader(inputStreamReader);

                        OutputStream outputStream = socket.getOutputStream();
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
                        PrintWriter bufferedWriter = new PrintWriter(outputStreamWriter, true);
                ) {
                    String s = stringReader.readLine();

                    if (s.endsWith("HTTP/1.1")) {
                        if (s.startsWith("GET")) {
                            if (s.startsWith("GET /sum")) {
                                if (s.startsWith("GET /sum?")) {
                                    String[] split = s.split("\\?");
                                    String s1 = split[1];
                                    String s2 = s1.replaceAll(" HTTP/1.1", "");
                                    String[] split1 = s2.split("&");
                                    int a = Integer.parseInt(split1[0].split("=")[1]);
                                    int b = Integer.parseInt(split1[1].split("=")[1]);

                                    int sum = a + b;
                                    //just wow...
                                    String response = "" +
                                                "HTTP/1.1 200 OK\n" +
//                                                "Date: Mon, 27 Jul 2009 12:28:53 GMT\n" +
                                                "Content-Length: " + ("" + sum).length() +
                                                "\n" +
                                                "Content-Type: text/html\n" +
//                                                "Connection: Closed\n" +
                                            "\n" +
                                            sum;
                                    bufferedWriter.println(response);
                                }
                            }
                        }
                    }
                }


            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            }
        }
    }
}
