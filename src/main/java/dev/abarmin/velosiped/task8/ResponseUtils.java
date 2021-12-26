package dev.abarmin.velosiped.task8;

public interface ResponseUtils {
    String createHttpResponseBodyWithBody(String responseStringJsonBody);

    String create401UnauthorizedAccessResponseWithBody(String textBody);

    String create500InternalServerErrorResponseWithBody(String textBody);
}
