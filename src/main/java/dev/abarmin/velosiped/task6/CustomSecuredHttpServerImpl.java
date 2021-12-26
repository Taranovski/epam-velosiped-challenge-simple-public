package dev.abarmin.velosiped.task6;

import dev.abarmin.velosiped.task2.Request;
import dev.abarmin.velosiped.task4.ResponseUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class CustomSecuredHttpServerImpl implements CustomSecuredHttpServer {

    private volatile ServerSocket serverSocket;
    private volatile boolean serverServesRequests;

    private ResponseUtils responseUtils = new ResponseUtils();
    private OneRequestServer oneRequestServer = new OneRequestServer();

    @Override
    public void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            serverServesRequests = true;

            runServerServingRequests();

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

    @Override
    public boolean securityFilter(String httpRequest) throws SecurityException {
        return false;
    }

    //todo awfully broken srp
    @Override
    public Request parseRequestParameters(String httpRequest) {
        try {
            Map<String, Object> intermediateResults = oneRequestServer.doHandlingAndRecordIntermediateData(
                    new ByteArrayInputStream(httpRequest.getBytes(StandardCharsets.US_ASCII)), new PrintWriter(new ByteArrayOutputStream()));
            return (Request) intermediateResults.get("parsed request");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String createHttpResponse(String responseBody) {
        return responseUtils.createHttpResponseBodyWithBody(responseBody);
    }

    private void runServerServingRequests() {
        new Thread(new BackgroundServingConnectionHandler()).start();
    }

    private class BackgroundServingConnectionHandler implements Runnable {

        @Override
        public void run() {
            serveRequestsUntilStopped();
        }

    }

    private void serveRequestsUntilStopped() {
        while (serverServesRequests) {
            serveOneRequest();
        }
    }

    private void serveOneRequest() {
        try {
            try (
                    Socket socket = serverSocket.accept();

                    InputStream inputStream = socket.getInputStream();

                    OutputStream outputStream = socket.getOutputStream();
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
                    PrintWriter bufferedWriter = new PrintWriter(outputStreamWriter, true);
            ) {
                oneRequestServer.doHandlingAndRecordIntermediateData(inputStream, bufferedWriter);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
