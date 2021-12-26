package dev.abarmin.velosiped.task10;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class OneRequestServerImpl implements OneRequestServer {
    @Inject
    private EndpointHandler endpointHandler;
    @Inject
    private VelosipedJsonAdapter velosipedJsonAdapter;
    @Inject
    private RequestUtils requestUtils;
    @Inject
    private ResponseUtils responseUtils;
    @Inject
    private SecurityChecker securityChecker;
    @Inject
    private RoutingHolder routingHolder;

    @Override
    public Map<String, Object> doHandlingAndRecordIntermediateData(InputStream inputStream, PrintWriter bufferedWriter) {
        Map<String, Object> intermediateResults = new HashMap<>();
        try {
            List<String> headerLines = requestUtils.getHeaders(inputStream);

            String method = requestUtils.getMethod(headerLines);
            String uriWithParams = requestUtils.getUriWithParams(headerLines, method);
            String strippedUri = requestUtils.getStrippedUri(uriWithParams);
            if (Objects.equals(strippedUri, "shutdown")) {
                String httpResponse = responseUtils.createHttpResponseBodyWithBody("shutdown", HTTPConstants.CONTENT_TYPE_TEXT_HTML);
                bufferedWriter.println(httpResponse);
                return Collections.emptyMap();
            }

            String authorizationValue = requestUtils.getAuthorizationValue(headerLines);

            boolean goodAuthorization = securityChecker.isGoodAuthorization(authorizationValue);
            intermediateResults.put("isAuthGood", goodAuthorization);

            if (goodAuthorization) {
                Map<HttpMethod, Map<String, MethodAndMethodParametersExtractor>> routings = routingHolder.getRoutings();

                Map<String, MethodAndMethodParametersExtractor> stringMethodAndMethodParametersExtractorMap = routings.get(HttpMethod.valueOf(method));

                if (stringMethodAndMethodParametersExtractorMap != null) {
                    MethodAndMethodParametersExtractor methodAndMethodParametersExtractor = stringMethodAndMethodParametersExtractorMap.get(strippedUri);
                    if (methodAndMethodParametersExtractor != null) {
                        List<ParameterExtractor> parameterExtractorList = methodAndMethodParametersExtractor.getParameterExtractorList();

                        List<Object> parameters = new ArrayList<>();
                        for (ParameterExtractor parameterExtractor : parameterExtractorList) {
                            Object parameter = parameterExtractor.extract(headerLines, inputStream, intermediateResults);
                            parameters.add(parameter);
                        }
                        Object response = methodAndMethodParametersExtractor.invoke(parameters);

                        String responseStringJsonBody;
                        String responseContentType;
                        if (Objects.equals(requestUtils.getContentType(headerLines), HTTPConstants.CONTENT_TYPE_APPLICATION_JSON)) {
                            responseStringJsonBody = velosipedJsonAdapter.writeAsJson(response);
                            responseContentType = HTTPConstants.CONTENT_TYPE_APPLICATION_JSON;
                        } else {
                            responseStringJsonBody = String.valueOf(response);
                            responseContentType = HTTPConstants.CONTENT_TYPE_TEXT_HTML;
                        }
                        System.out.println(responseStringJsonBody);

                        String httpResponse = responseUtils.createHttpResponseBodyWithBody(responseStringJsonBody, responseContentType);
                        bufferedWriter.println(httpResponse);
                    } else {
                        String httpResponse = responseUtils.create500InternalServerErrorResponseWithBody("no route " + strippedUri);
                        bufferedWriter.println(httpResponse);
                    }
                } else {
                    String httpResponse = responseUtils.create500InternalServerErrorResponseWithBody("no method " + method);
                    bufferedWriter.println(httpResponse);
                }

            } else {
                String httpResponse = responseUtils.create401UnauthorizedAccessResponseWithBody("Unauthorized access");
                bufferedWriter.println(httpResponse);
            }

        } catch (Exception e) {
            String httpResponse = responseUtils.create500InternalServerErrorResponseWithBody("exception " + e);
            bufferedWriter.println(httpResponse);
        }
        return intermediateResults;
    }

}
