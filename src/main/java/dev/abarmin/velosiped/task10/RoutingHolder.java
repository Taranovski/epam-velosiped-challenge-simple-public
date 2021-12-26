package dev.abarmin.velosiped.task10;

import java.util.Map;

public interface RoutingHolder {
    Map<HttpMethod, Map<String, MethodAndMethodParametersExtractor>> getRoutings();
}
