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
package io.verifnow.spring.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import io.verifnow.core.client.VerifNowClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.reactive.function.client.WebClient;

class ValidationAutoConfigurationTest {

  private final ApplicationContextRunner runner = new ApplicationContextRunner()
      .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
      .withPropertyValues("verifnow.api.api-key=test-key");

  @Test
  void createsTheClient_whenNoWebClientBuilderIsAvailable() {
    // Given an application with no WebClient.Builder bean, which is what Spring Boot 4 gives an
    // application that does not declare spring-boot-webclient. Up to 2.9.0 this failed at startup.
    // When the context starts
    // Then the client is still created
    runner.run(context -> {
      assertThat(context).hasNotFailed();
      assertThat(context).hasSingleBean(VerifNowClient.class);
    });
  }

  @Test
  void usesTheApplicationsWebClientBuilder_whenOneIsAvailable() {
    // Given an application that provides its own WebClient.Builder
    WebClient.Builder applicationBuilder = spy(WebClient.builder());

    // When the context starts
    runner.withBean(WebClient.Builder.class, () -> applicationBuilder).run(context -> {
      // Then the client is built from it, so the application's codecs and filters apply
      assertThat(context).hasSingleBean(VerifNowClient.class);
      verify(applicationBuilder).build();
    });
  }
}
