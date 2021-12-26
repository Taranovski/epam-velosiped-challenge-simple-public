package dev.abarmin.velosiped.task10;

public interface ResponseUtils {
    String createHttpResponseBodyWithBody(String responseStringJsonBody, String contentType);

    String create401UnauthorizedAccessResponseWithBody(String textBody);

    String create500InternalServerErrorResponseWithBody(String textBody);
}
