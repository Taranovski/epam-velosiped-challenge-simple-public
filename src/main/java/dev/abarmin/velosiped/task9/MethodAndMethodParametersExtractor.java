package dev.abarmin.velosiped.task9;

import java.lang.reflect.Method;
import java.util.List;

public class MethodAndMethodParametersExtractor {
    private final Method method;
    private final Object object;
    private final List<ParameterExtractor> parameterExtractorList;

    public MethodAndMethodParametersExtractor(Method method, Object object, List<ParameterExtractor> parameterExtractorList) {
        this.method = method;
        this.object = object;
        this.parameterExtractorList = parameterExtractorList;
    }

    public Object invoke(List<Object> parameters) {
        try {
            return method.invoke(object, parameters.toArray(new Object[]{}));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<ParameterExtractor> getParameterExtractorList() {
        return parameterExtractorList;
    }
}
