package dev.abarmin.velosiped.task10;

import dev.abarmin.velosiped.helper.VelosipedHelper;
import dev.abarmin.velosiped.task8.ClassByPackageScanner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Aleksandr Barmin
 */
public class VelosipedTask10Test {
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

  @ParameterizedTest
  @CsvSource({
          "1,2",
          "10,20",
          "-1,-2"
  })
  void check_calculation_uri_params(int a, int b) throws Exception {
    final URL url = new URL("http://localhost:1234/sum?a=" + a + "&b=" + b);
    final URLConnection connection = url.openConnection();
    connection.setDoOutput(true);
    connection.setRequestProperty("Authorization", "Bearer " +
            new String(Base64.getEncoder().encode("Batman".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
    try (final InputStream stream = connection.getInputStream()) {
      final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
      final String response = reader.readLine();
      assertEquals(a + b, Integer.parseInt(response));
    }
  }

  @Test
  void check_thereIsAClassAnnotatedWithControllerAnnotation() {
    List<Class<?>> classes = new ClassByPackageScanner().getClasses("dev.abarmin.velosiped.task10");
    final Set<Class<?>> classesWithAnnotatedMethods = classes
        .stream()
        .filter(type -> hasAnnotatedMethods(type, RequestMapping.class))
        .collect(Collectors.toSet());

    assertThat(classesWithAnnotatedMethods)
        .withFailMessage("There are no classes with methods annotated with @RequestMapping " +
            "annotation in the package dev.abarmin.velosiped.task10")
        .isNotEmpty();

    final Set<Method> annotatedMethods = classesWithAnnotatedMethods.stream()
        .flatMap(type -> Arrays.stream(type.getDeclaredMethods()))
        .filter(method -> method.isAnnotationPresent(RequestMapping.class))
        .filter(method -> hasAnnotatedParameter(method, QueryParameter.class))
        .collect(Collectors.toSet());

    assertThat(annotatedMethods)
        .withFailMessage("There are no parameters annotated with @QueryParameter in methods " +
            "annotated with @RequestMapping in the package dev.abarmin.velosiped.task10")
        .isNotEmpty();
  }

  private boolean hasAnnotatedParameter(Method method, Class<? extends Annotation> annotation) {
    for (Annotation[] parameterAnnotations : method.getParameterAnnotations()) {
      for (Annotation parameterAnnotation : parameterAnnotations) {
        if (parameterAnnotation.annotationType() == annotation) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean hasAnnotatedMethods(Class<?> classToCheck,
                                      Class<? extends Annotation> annotation) {
    for (Method method : classToCheck.getDeclaredMethods()) {
      if (method.isAnnotationPresent(annotation)) {
        return true;
      }
    }
    return false;
  }
}
