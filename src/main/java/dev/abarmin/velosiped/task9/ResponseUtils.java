package dev.abarmin.velosiped.task9;

public interface ResponseUtils {
    String createHttpResponseBodyWithBody(String responseStringJsonBody);

    String create401UnauthorizedAccessResponseWithBody(String textBody);

    String create500InternalServerErrorResponseWithBody(String textBody);
}
