package dev.abarmin.velosiped.task10;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class QueryParameterExtractor implements ParameterExtractor {
    private final String uriParameterName;
    private final Class<?> parameterType;

    public QueryParameterExtractor(String uriParameterName, Class<?> parameterType) {
        this.uriParameterName = uriParameterName;
        this.parameterType = parameterType;
    }

    @Inject
    private RequestUtils requestUtils;

    @Override
    public Object extract(List<String> headerLines, InputStream inputStream, Map<String, Object> intermediateResults) {

        String method = requestUtils.getMethod(headerLines);
        String uriWithParams = requestUtils.getUriWithParams(headerLines, method);
        //not required right now
        Map<String, String> uriParams = requestUtils.getUriParams(uriWithParams);
        String uriParameterValue = uriParams.get(uriParameterName);
        if (parameterType == int.class) {
            return Integer.parseInt(uriParameterValue);
        }
        if (parameterType == String.class) {
            return uriParameterValue;
        }
        throw new RuntimeException();
    }
}
