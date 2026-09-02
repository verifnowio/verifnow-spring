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

import org.junit.jupiter.api.Test;

class ValidationPropertiesTest {

  @Test
  void defaultBaseUrl_pointsAtTheHostThatActuallyResolves() {
    // Regression guard for 2.1.1. Releases up to 2.1.0 shipped "https://api.verifnow.com",
    // a host with no DNS record, so the SDK could never reach the API out of the box.
    assertThat(new ValidationProperties().getBaseUrl()).isEqualTo("https://api.verifnow.io");
  }

  @Test
  void defaults_areFailOpenWithCachingEnabled() {
    ValidationProperties props = new ValidationProperties();

    assertThat(props.isFailOnError()).isFalse();
    assertThat(props.isCacheEnabled()).isTrue();
    assertThat(props.getTimeoutMs()).isEqualTo(1500);
    assertThat(props.getCacheTtlSeconds()).isEqualTo(60);
  }
}
