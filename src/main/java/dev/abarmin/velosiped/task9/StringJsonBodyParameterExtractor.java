package dev.abarmin.velosiped.task9;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StringJsonBodyParameterExtractor implements ParameterExtractor {

    private final Class<?> aClass;

    public StringJsonBodyParameterExtractor(Class aClass) {
        this.aClass = aClass;
    }

    @Inject
    private VelosipedJsonAdapter velosipedJsonAdapter;
    @Inject
    private RequestUtils requestUtils;

    @Override
    public Object extract(List<String> headerLines, InputStream inputStream, Map<String, Object> intermediateResults) {
        int contentLength = requestUtils.getContentLength(headerLines);
        String contentType = requestUtils.getContentType(headerLines);
        //todo bytes?
        if (Objects.equals(contentType, HTTPConstants.CONTENT_TYPE_APPLICATION_JSON)) {
            String stringJsonBody = requestUtils.getBodyAsString(inputStream, contentLength);

            System.out.println(stringJsonBody);
            Object request = velosipedJsonAdapter.parse(stringJsonBody, aClass);
            intermediateResults.put("parsed request", request);
            return request;
        } else {
            throw new RuntimeException();
        }
    }
}
