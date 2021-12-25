package dev.abarmin.velosiped.task4;

import java.time.Instant;

public class ResponseUtils {
    public String createHttpResponseBodyWithBody(String responseStringJsonBody) {
        String httpResponse = "" +
                "HTTP/1.1 200 OK\n" +
                HTTPConstants.CONTENT_LENGTH_HEADER + responseStringJsonBody.length() + "\n" +
                HTTPConstants.CONTENT_TYPE_HEADER + HTTPConstants.CONTENT_TYPE_APPLICATION_JSON + "\n" +
                "Server: VelosipedServer\n" +
                "Date: \n" + Instant.now().toString() + "\n" +
                "Content-Type: " + HTTPConstants.CONTENT_TYPE_APPLICATION_JSON + "\n" +
                "\n" +
                responseStringJsonBody;
        return httpResponse;
    }

    public String create401UnauthorizedAccessResponseWithBody(String textBody) {
        String httpResponse = "" +
                "HTTP/1.1 401 Unauthorized\n" +
                HTTPConstants.CONTENT_LENGTH_HEADER + textBody.length() + "\n" +
                HTTPConstants.CONTENT_TYPE_HEADER + HTTPConstants.CONTENT_TYPE_TEXT_HTML + "\n" +
                "Server: VelosipedServer\n" +
                "Date: \n" + Instant.now().toString() + "\n" +
                "Content-Type: " + HTTPConstants.CONTENT_TYPE_TEXT_HTML + "\n" +
                "\n" +
                textBody;
        return httpResponse;
    }
}
