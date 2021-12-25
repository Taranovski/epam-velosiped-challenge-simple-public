package dev.abarmin.velosiped.task6;

import dev.abarmin.velosiped.task2.EndpointHandler;
import dev.abarmin.velosiped.task2.Request;
import dev.abarmin.velosiped.task2.Response;
import dev.abarmin.velosiped.task3.VelosipedJsonAdapter;
import dev.abarmin.velosiped.task3.VelosipedJsonAdapterImpl;
import dev.abarmin.velosiped.task4.HTTPConstants;
import dev.abarmin.velosiped.task4.RequestUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CustomSecuredHttpServerImpl implements CustomSecuredHttpServer {

    private volatile ServerSocket serverSocket;
    private volatile boolean serverServesRequests;

    private EndpointHandler endpointHandler = new EndpointHandler();
    private VelosipedJsonAdapter velosipedJsonAdapter = new VelosipedJsonAdapterImpl();
    private RequestUtils requestUtils = new RequestUtils();
    private ResponseUtils responseUtils = new ResponseUtils();
    private SecurityChecker securityChecker = new SecurityChecker();

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
            Map<String, Object> intermediateResults = doHandlingAndRecordIntermediateData(
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
        new Thread(new MyHandrittenConnectionHandler()).start();
    }

    private class MyHandrittenConnectionHandler implements Runnable {

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
                doHandlingAndRecordIntermediateData(inputStream, bufferedWriter);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> doHandlingAndRecordIntermediateData(InputStream inputStream, PrintWriter bufferedWriter) throws IOException {
        Map<String, Object> intermediateResults = new HashMap<>();
        List<String> headerLines = requestUtils.getHeaders(inputStream);

        String method = requestUtils.getMethod(headerLines);
        String uriWithParams = requestUtils.getUriWithParams(headerLines, method);
        //not required right now
        Map<String, String> uriParams = requestUtils.getUriParams(uriWithParams);
        String strippedUri = requestUtils.getStrippedUri(uriWithParams);

        String authorizationValue = requestUtils.getAuthorizationValue(headerLines);

        boolean goodAuthorization = securityChecker.isGoodAuthorization(authorizationValue);
        intermediateResults.put("isAuthGood", goodAuthorization);

        if (goodAuthorization) {

            if (Objects.equals(method, HTTPConstants.HTTP_METHOD_POST)) {
                if (Objects.equals(strippedUri, "/sum-post")) {
                    int contentLength = requestUtils.getContentLength(headerLines);
                    String contentType = requestUtils.getContentType(headerLines);
                    //todo bytes?
                    if (Objects.equals(contentType, HTTPConstants.CONTENT_TYPE_APPLICATION_JSON)) {
                        String stringJsonBody = requestUtils.getBodyAsString(inputStream, contentLength);

                        System.out.println(stringJsonBody);
                        Request request = velosipedJsonAdapter.parse(stringJsonBody, Request.class);
                        intermediateResults.put("parsed request", request);

                        Response response = endpointHandler.calculateSum(request);

                        String responseStringJsonBody = velosipedJsonAdapter.writeAsJson(response);
                        System.out.println(responseStringJsonBody);

                        String httpResponse = responseUtils.createHttpResponseBodyWithBody(responseStringJsonBody);
                        bufferedWriter.println(httpResponse);
                    }
                }
            }
        } else {
            String httpResponse = responseUtils.create401UnauthorizedAccessResponseWithBody("Unauthorized access");
            bufferedWriter.println(httpResponse);
        }

        return intermediateResults;
    }

}
