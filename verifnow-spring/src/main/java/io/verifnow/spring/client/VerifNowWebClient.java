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

import io.verifnow.core.client.VerifNowClient;
import io.verifnow.core.client.ValidationResult;
import io.verifnow.spring.config.ValidationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class VerifNowWebClient implements VerifNowClient {

  private static final Logger logger = LoggerFactory.getLogger(VerifNowWebClient.class);

  /** Path both the blocking and the async call must use. */
  private static final String VALIDATE_PATH = "/api/v1/validate/{rule}";

  private final WebClient webClient;
  private final ValidationProperties props;

  private static final String SDK_HEADER_NAME = "X-VerifNow-SDK";
  private static final String SDK_ID = "java";
  private static final String SDK_VERSION = resolveVersion();
  private static final String SDK_HEADER_VALUE = SDK_ID + "/" + SDK_VERSION;

  public VerifNowWebClient(WebClient.Builder builder, ValidationProperties props) {
    this.props = props;
    WebClient.Builder b = builder.baseUrl(props.getBaseUrl());
    if (props.getApiKey() != null) {
      b.defaultHeader("X-API-KEY", props.getApiKey());
    }
    // Add SDK identification header to all outgoing calls
    b.defaultHeader(SDK_HEADER_NAME, SDK_HEADER_VALUE);
    this.webClient = b.build();
  }

  @Override
  public ValidationResult validate(String rule, String value) {
    try {
      // Blocking because ConstraintValidator.isValid is synchronous
      return requestValidation(rule, value).block();
    } catch (WebClientResponseException wex) {
      throw wex;
    } catch (Exception ex) {
      if (props.isFailOnError()) {
        // fail closed: throw runtime to fail validation flow
        throw new RuntimeException("VerifNow API error", ex);
      }

      // Fail open: accept the value unverified so an outage does not block the caller's own
      // flow. Logged at WARN because the alternative — degrading silently — makes the outage
      // invisible until someone audits the data that got through.
      logger.warn(
          "VerifNow API unreachable at {} while validating rule '{}'; accepting the value "
              + "unverified (verifnow.api.failOnError=false). Cause: {}",
          props.getBaseUrl(), rule, ex.toString());

      ValidationResult r = new ValidationResult();
      r.setValid(true);
      r.setMessage("fallback-permit");
      return r;
    }
  }

  @Override
  public CompletableFuture<ValidationResult> validateAsync(String rule, String value) {
    return requestValidation(rule, value).toFuture();
  }

  private Mono<ValidationResult> requestValidation(String rule, String value) {
    return webClient.post()
        .uri(uriBuilder -> uriBuilder.path(VALIDATE_PATH).build(rule))
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(new RequestPayload(value))
        .retrieve()
        .bodyToMono(ValidationResult.class)
        .timeout(Duration.ofMillis(props.getTimeoutMs()));
  }

  private static class RequestPayload {
    private String value;

    public RequestPayload(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  private static String resolveVersion() {
    // Prefer Implementation-Version from manifest
    String impl = VerifNowWebClient.class.getPackage().getImplementationVersion();
    if (impl != null && !impl.isBlank()) {
      return impl;
    }
    // Fallback to pom.properties if available
    String pomPath = "/META-INF/maven/io.verifnow/verifnow-spring/pom.properties"; // adjust if group/artifact differ
    try (InputStream in = VerifNowWebClient.class.getResourceAsStream(pomPath)) {
      if (in != null) {
        Properties p = new Properties();
        p.load(in);
        String v = p.getProperty("version");
        if (v != null && !v.isBlank()) {
          return v;
        }
      }
    } catch (IOException ignored) {
      // ignore and fallback
    }

    return null;
  }
}
