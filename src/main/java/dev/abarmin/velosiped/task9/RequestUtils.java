package dev.abarmin.velosiped.task9;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface RequestUtils {
    String getAuthorizationValue(List<String> headerLines);

    String getStrippedUri(String uriWithParams);

    String getContentType(List<String> headerLines);

    Map<String, String> getUriParams(String uriWithParams);

    String getUriWithParams(List<String> headerLines, String method);

    String getMethod(List<String> headerLines);

    int getContentLength(List<String> headerLines);

    List<String> getHeaders(InputStream inputStream);

    String getBodyAsString(InputStream inputStream, int contentLength);
}
