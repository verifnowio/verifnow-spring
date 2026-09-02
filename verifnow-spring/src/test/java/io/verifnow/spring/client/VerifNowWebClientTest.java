/**
 * Copyright (c) 2025-2025 VerifNow
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.verifnow.spring.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.verifnow.core.client.ValidationResult;
import io.verifnow.spring.config.ValidationProperties;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class VerifNowWebClientTest {

  private static final String VALID_BODY = """
      {"valid":true,"message":"Valid email address","normalizedValue":"user@example.com",\
      "originalValue":"USER@Example.Com","validationLevel":"STANDARD"}""";

  private MockWebServer server;

  @BeforeEach
  void startServer() throws IOException {
    server = new MockWebServer();
    server.start();
  }

  @AfterEach
  void stopServer() throws IOException {
    server.shutdown();
  }

  private VerifNowWebClient clientFor(ValidationProperties props) {
    return new VerifNowWebClient(WebClient.builder(), props);
  }

  private ValidationProperties propsPointingAtServer() {
    ValidationProperties props = new ValidationProperties();
    props.setBaseUrl(server.url("/").toString());
    props.setApiKey("test-api-key");
    return props;
  }

  @Test
  void validate_postsToTheValidateEndpointWithKeyAndSdkHeader() throws Exception {
    server.enqueue(jsonResponse(VALID_BODY));

    ValidationResult result = clientFor(propsPointingAtServer()).validate("email",
        "USER@Example.Com");

    RecordedRequest request = server.takeRequest(2, TimeUnit.SECONDS);
    assertThat(request).isNotNull();
    assertThat(request.getMethod()).isEqualTo("POST");
    assertThat(request.getPath()).isEqualTo("/api/v1/validate/email");
    assertThat(request.getHeader("X-API-KEY")).isEqualTo("test-api-key");
    assertThat(request.getHeader("X-VerifNow-SDK")).startsWith("java/");
    assertThat(request.getBody().readUtf8()).isEqualTo("{\"value\":\"USER@Example.Com\"}");

    assertThat(result.isValid()).isTrue();
    assertThat(result.getNormalizedValue()).isEqualTo("user@example.com");
    assertThat(result.getValidationLevel()).isEqualTo("STANDARD");
  }

  @Test
  void validateAsync_usesTheSameEndpointAsValidate() throws Exception {
    // Regression guard for 2.1.1: validateAsync used to call "/api/v1/{rule}", missing the
    // "validate" segment, so every asynchronous call 404'd.
    server.enqueue(jsonResponse(VALID_BODY));

    ValidationResult result = clientFor(propsPointingAtServer())
        .validateAsync("email", "user@example.com")
        .get(5, TimeUnit.SECONDS);

    RecordedRequest request = server.takeRequest(2, TimeUnit.SECONDS);
    assertThat(request).isNotNull();
    assertThat(request.getPath()).isEqualTo("/api/v1/validate/email");
    assertThat(result.isValid()).isTrue();
  }

  @Test
  void validate_routesEachRuleToItsOwnEndpoint() throws Exception {
    for (String rule : new String[] {"email", "phone", "iban", "vat", "nas", "ssn", "nif"}) {
      server.enqueue(jsonResponse(VALID_BODY));
      clientFor(propsPointingAtServer()).validate(rule, "value");

      RecordedRequest request = server.takeRequest(2, TimeUnit.SECONDS);
      assertThat(request).isNotNull();
      assertThat(request.getPath()).isEqualTo("/api/v1/validate/" + rule);
    }
  }

  @Test
  void validate_failsOpenWhenTheApiIsUnreachable() throws IOException {
    ValidationProperties props = propsPointingAtServer();
    props.setTimeoutMs(200);
    server.shutdown(); // nothing is listening any more

    ValidationResult result = clientFor(props).validate("email", "user@example.com");

    // Documented behaviour: accept unverified rather than block the caller's flow.
    assertThat(result.isValid()).isTrue();
    assertThat(result.getMessage()).isEqualTo("fallback-permit");
  }

  @Test
  void validate_failsClosedWhenFailOnErrorIsSet() throws IOException {
    ValidationProperties props = propsPointingAtServer();
    props.setTimeoutMs(200);
    props.setFailOnError(true);
    server.shutdown();

    assertThatThrownBy(() -> clientFor(props).validate("email", "user@example.com"))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("VerifNow API error");
  }

  private static MockResponse jsonResponse(String body) {
    return new MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(body);
  }
}
