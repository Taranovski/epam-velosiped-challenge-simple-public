package dev.abarmin.velosiped.task3;

import dev.abarmin.velosiped.task2.EndpointHandler;
import dev.abarmin.velosiped.task2.Request;
import dev.abarmin.velosiped.task2.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class VelosipedTask3Impl implements VelosipedTask3 {

    public static final String DEFAULT_PROTOCOL = " HTTP/1.1";
    public static final String HTTP_METHOD_POST = "POST";
    public static final String CONTENT_LENGTH_HEADER = "Content-Length: ";
    public static final String CONTENT_TYPE_HEADER = "Content-Type: ";
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    private volatile ServerSocket serverSocket;
    private volatile boolean serverServesRequests;

    @Override
    public void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            serverServesRequests = true;

            new Thread(new VelosipedTask3Impl.MyHandrittenConnectionHandler()).start();

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

    private final EndpointHandler endpointHandler = new EndpointHandler();

    private VelosipedJsonAdapter velosipedJsonAdapter = new VelosipedJsonAdapterImpl();

    private class MyHandrittenConnectionHandler implements Runnable {

        @Override
        public void run() {
            while (serverServesRequests) {

                try {
                    try (
                            Socket socket = serverSocket.accept();

                            InputStream inputStream = socket.getInputStream();

                            OutputStream outputStream = socket.getOutputStream();
                            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
                            PrintWriter bufferedWriter = new PrintWriter(outputStreamWriter, true);
                    ) {
                        List<String> headerLines = getHeaders(inputStream);

                        String method = getMethod(headerLines);
                        String uriWithParams = getUriWithParams(headerLines, method);
                        //not required right now
                        Map<String, String> uriParams = getUriParams(uriWithParams);
                        String strippedUri = getStrippedUri(uriWithParams);

                        if (Objects.equals(method, HTTP_METHOD_POST)) {
                            if (Objects.equals(strippedUri, "/sum-post")) {
                                int contentLength = getContentLength(headerLines);
                                String contentType = getContentType(headerLines);
                                //todo bytes?
                                if (Objects.equals(contentType, CONTENT_TYPE_APPLICATION_JSON)) {
                                    String stringJsonBody = getBodyAsString(inputStream, contentLength);

                                    System.out.println(stringJsonBody);
                                    Request request = velosipedJsonAdapter.parse(stringJsonBody, Request.class);

                                    Response response = endpointHandler.calculateSum(request);

                                    String responseStringJsonBody = velosipedJsonAdapter.writeAsJson(response);
                                    System.out.println(responseStringJsonBody);

                                    String httpResponse = "" +
                                            "HTTP/1.1 200 OK\n" +
                                            CONTENT_LENGTH_HEADER + (responseStringJsonBody).length() + "\n" +
                                            CONTENT_TYPE_HEADER + CONTENT_TYPE_APPLICATION_JSON + "\n" +
//                                            "Connection: Closed\n" +
                                            "\n" +
                                            responseStringJsonBody;
                                    bufferedWriter.println(httpResponse);
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

    private String getStrippedUri(String uriWithParams) {
        String[] split = uriWithParams.split("\\?");
        return split[0];
    }

    private String getContentType(List<String> headerLines) {
        String contentType = null;
        for (String s : headerLines) {
            if (s.startsWith(CONTENT_TYPE_HEADER)) {
                contentType = s.replace(CONTENT_TYPE_HEADER, "");
            }
        }
        return contentType;
    }

    private Map<String, String> getUriParams(String uriWithParams) {

        String[] split = uriWithParams.split("\\?");
        if (split.length > 1) {
            String s1 = split[1];
            String[] split1 = s1.split("&");

            Map<String, String> uriParams = new HashMap<>();
            for (String s : split1) {
                String[] split2 = s.split("=");

                uriParams.put(split2[0], split2[1]);
            }
            return uriParams;
        } else {
            return Collections.emptyMap();
        }
    }

    private String getUriWithParams(List<String> headerLines, String method) {
        return headerLines.get(0).replace(method + " ", "").replace(DEFAULT_PROTOCOL, "");
    }

    private String getMethod(List<String> headerLines) {
        return headerLines.get(0).split(" ")[0];
    }

    private String getBodyAsString(InputStream inputStream, int contentLength) throws IOException {
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < contentLength; i++) {
            int c = inputStream.read();
            body.append((char) c);
        }

        return body.toString();
    }

    private int getContentLength(List<String> headerLines) {
        int contentLength = -1;
        for (String s : headerLines) {
            if (s.startsWith(CONTENT_LENGTH_HEADER)) {
                contentLength = Integer.parseInt(s.replace(CONTENT_LENGTH_HEADER, ""));
            }
        }
        return contentLength;
    }

    private List<String> getHeaders(InputStream inputStream) throws IOException {
        List<String> headerLines = new ArrayList<>();
        String currentLine = null;
        String previousLine = null;
        String previous1Line = null;
        while (true) {
            StringBuilder body = new StringBuilder();
            while (true) {
                int c = inputStream.read();
                if (c == '\n' || c == '\r') {
                    break;
                }
                body.append((char) c);
            }

            previous1Line = previousLine;
            previousLine = currentLine;
            currentLine = body.toString();

            if ("".equals(currentLine) && "".equals(previousLine) && "".equals(previous1Line)) {
                break;
            }
            if (!"".equals(currentLine)) {
                headerLines.add(currentLine);
            }
        }
        return headerLines;
    }
}

