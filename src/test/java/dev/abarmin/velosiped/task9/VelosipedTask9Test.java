package dev.abarmin.velosiped.task9;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;

import dev.abarmin.velosiped.helper.VelosipedHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.reflections.Reflections;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Aleksandr Barmin
 */
public class VelosipedTask9Test {
  private DIContainer diContainer = VelosipedHelper.getInstance(DIContainer.class);
  private CustomSecuredHttpServer server;

  @BeforeEach
  void setUp() {
    diContainer.init();
    server = diContainer.getBean(CustomSecuredHttpServer.class);
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
    connection.setRequestProperty("Authorization", "Bearer " +
            new String(Base64.getEncoder().encode("Batman".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));

    final String requestBody = "{\"arg1\": " + a + ", \"arg2\": " + b + "}";
    try (final OutputStream outputStream = connection.getOutputStream()) {
      outputStream.write(requestBody.getBytes(StandardCharsets.UTF_8));
    }

    try (final InputStream stream = connection.getInputStream()) {
      final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
      final String response = reader.readLine();

      final String expectedResult = "{\"result\":" + (a + b) + "}";
      assertThat(response)
              .withFailMessage("Unexpected response")
              .isEqualTo(expectedResult);
    }
  }

  @Test
  void checking_classesAnnotatedWithControllerAnnotation() {
    final Reflections scanner = new Reflections("dev.abarmin.velosiped.task9");
    final Set<Class<?>> controllers = scanner.getTypesAnnotatedWith(Controller.class);

    assertThat(controllers)
        .withFailMessage("No classes annotated with @Controller in the package " +
            "with name dev.abarmin.velosiped.task9")
        .isNotEmpty();
  }

  @Test
  void checking_classesAnnotatedWithServiceAnnotation() {
    final Reflections scanner = new Reflections("dev.abarmin.velosiped.task9");
    final Set<Class<?>> service = scanner.getTypesAnnotatedWith(Service.class);

    assertThat(service)
        .withFailMessage("No classes annotated with @Service in the package " +
            "with name dev.abarmin.velosiped.task9")
        .isNotEmpty();
  }
}
