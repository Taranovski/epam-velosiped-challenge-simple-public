package dev.abarmin.velosiped.task8;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface ParameterExtractor {
    Object extract(List<String> headerLines, InputStream inputStream, Map<String, Object> intermediateResults);
}
