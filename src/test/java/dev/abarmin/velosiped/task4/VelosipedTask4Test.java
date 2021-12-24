package dev.abarmin.velosiped.task4;

import dev.abarmin.velosiped.helper.VelosipedHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import dev.abarmin.velosiped.task2.Request;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Aleksandr Barmin
 */
class VelosipedTask4Test {

    private CustomHttpServer server = VelosipedHelper.getInstance(CustomHttpServer.class);

    @BeforeEach
    void setUp() {
        server.startServer(1234);
    }

    @AfterEach
    void tearDown() {
        server.stopServer();
    }

    @ParameterizedTest
    @CsvSource({
            "1,2",
            "10,20",
            "-1,-2"
    })
    void check_calculation(int a, int b) throws Exception {
        final URL url = new URL("http://localhost:1234/sum-post");
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");

        final String requestBody = "{\"arg1\": " + a + ", \"arg2\": " + b + "}";
        try (final OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(requestBody.getBytes(StandardCharsets.UTF_8));
        }

        try (final InputStream stream = connection.getInputStream()) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            final String response = reader.readLine();

            final String expectedResult = "{\"result\":" + (a + b) + "}";
            assertEquals(expectedResult, response);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "1,2",
            "10,20",
            "-1,-2"
    })
    void check_httpRequest(int a, int b) {
        Request request = server.parseRequestParameters("POST /post-sum HTTP/1.1\n" +
                "Host: localhost:1234\n" +
                "Content-Type: application/json\n" +
                "Content-Length: " +
                ((String.valueOf(a) +String.valueOf(b)).length() + 31) +
                "\n" +
                "\n" +
                "{\n" +
                "    \"arg1\": " + a + ",\n" +
                "    \"arg2\": " + b + " \n" +
                "}");
        assertEquals(request, new Request(a, b));
    }

    /*
    HTTP/1.1 200 OK
    Date: Mon, 27 Jul 2009 12:28:53 GMT
    Server: VelosipedServer/2.2.14 (Win64)
    Last-Modified: Wed, 22 Jul 2009 19:15:56 GMT
    Content-Length: 88
    Content-Type: text/html
    Connection: Closed
     */
    @ParameterizedTest
    @CsvSource({
            "1,2",
            "10,20",
            "-1,-2"
    })
    void check_httpResponse(int a, int b) {
        String response = server.createHttpResponse("{\"result\":" + (a + b) + "}");
        assertTrue(response.contains("HTTP/1.1 200 OK"));
        assertTrue(response.contains("Date:"));
        assertTrue(response.contains("Server: VelosipedServer"));
        assertTrue(response.contains("Content-Length: "));
        assertTrue(response.contains("Content-Type:"));
        assertTrue(response.contains("{\"result\":" + (a + b) + "}"));

    }
}