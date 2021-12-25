package dev.abarmin.velosiped.task4;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestUtils {

    public String getAuthorizationValue(List<String> headerLines) {
        String authorizationValue = null;
        for (String s : headerLines) {
            if (s.startsWith(HTTPConstants.AUTHORIZATION_HEADER)) {
                authorizationValue = s.replace(HTTPConstants.AUTHORIZATION_HEADER, "")
                        .replace(HTTPConstants.AUTHORIZATION_HEADER_BEARER, "");
            }
        }
        return authorizationValue;
    }

    public String getStrippedUri(String uriWithParams) {
        String[] split = uriWithParams.split("\\?");
        return split[0];
    }

    public String getContentType(List<String> headerLines) {
        String contentType = null;
        for (String s : headerLines) {
            if (s.startsWith(HTTPConstants.CONTENT_TYPE_HEADER)) {
                contentType = s.replace(HTTPConstants.CONTENT_TYPE_HEADER, "");
            }
        }
        return contentType;
    }

    public Map<String, String> getUriParams(String uriWithParams) {

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

    public String getUriWithParams(List<String> headerLines, String method) {
        return headerLines.get(0).replace(method + " ", "").replace(HTTPConstants.DEFAULT_PROTOCOL, "");
    }

    public String getMethod(List<String> headerLines) {
        return headerLines.get(0).split(" ")[0];
    }

    public int getContentLength(List<String> headerLines) {
        int contentLength = -1;
        for (String s : headerLines) {
            if (s.startsWith(HTTPConstants.CONTENT_LENGTH_HEADER)) {
                contentLength = Integer.parseInt(s.replace(HTTPConstants.CONTENT_LENGTH_HEADER, ""));
            }
        }
        return contentLength;
    }

    public List<String> getHeaders(InputStream inputStream) throws IOException {
        List<String> headerLines = new ArrayList<>();
        String currentLine = null;
        int newLineCounter = 0;
        while (true) {
            StringBuilder body = new StringBuilder();
            while (true) {
                int c = inputStream.read();
                if (c == '\r') {
                    continue;
                }
                if (c == '\n') {
                    newLineCounter++;
                    break;
                }
                body.append((char) c);
                newLineCounter = 0;
            }

            currentLine = body.toString();

            if (newLineCounter == 2) {
                break;
            }
            if (!"".equals(currentLine)) {
                headerLines.add(currentLine);
            }
        }
        return headerLines;
    }

    public String getBodyAsString(InputStream inputStream, int contentLength) throws IOException {
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < contentLength; i++) {
            int c = inputStream.read();
            body.append((char) c);
        }

        return body.toString();
    }
}
