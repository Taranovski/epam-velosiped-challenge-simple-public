package dev.abarmin.velosiped.task6;

import dev.abarmin.velosiped.task2.EndpointHandler;
import dev.abarmin.velosiped.task2.Request;
import dev.abarmin.velosiped.task2.Response;
import dev.abarmin.velosiped.task3.VelosipedJsonAdapter;
import dev.abarmin.velosiped.task3.VelosipedJsonAdapterImpl;
import dev.abarmin.velosiped.task4.HTTPConstants;
import dev.abarmin.velosiped.task4.RequestUtils;
import dev.abarmin.velosiped.task4.ResponseUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OneRequestServer {

    private EndpointHandler endpointHandler = new EndpointHandler();
    private VelosipedJsonAdapter velosipedJsonAdapter = new VelosipedJsonAdapterImpl();
    private RequestUtils requestUtils = new RequestUtils();
    private ResponseUtils responseUtils = new ResponseUtils();
    private SecurityChecker securityChecker = new SecurityChecker();

    public Map<String, Object> doHandlingAndRecordIntermediateData(InputStream inputStream, PrintWriter bufferedWriter) throws IOException {
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
