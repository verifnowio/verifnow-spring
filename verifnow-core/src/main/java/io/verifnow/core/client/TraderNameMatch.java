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
package io.verifnow.core.client;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Whether a supplied company name belongs to a VAT number's registered holder.
 *
 * @since 2.9.0
 */
public enum TraderNameMatch {

  /** The name matches the registered holder. */
  MATCH,

  /** The name does not match. A question for a human, not proof of fraud — trading names and group companies differ legitimately. */
  MISMATCH,

  /** No answer is possible: the member state neither publishes nor checks the name (Germany), VIES could not be reached, or the number is not registered. */
  NOT_AVAILABLE;

  /**
   * Reads the API value, returning {@code null} for one this SDK version does not know, so a value
   * added to the API later cannot make the whole response unreadable.
   */
  @JsonCreator
  public static TraderNameMatch fromValue(String value) {
    if (value == null) {
      return null;
    }
    for (TraderNameMatch candidate : values()) {
      if (candidate.name().equals(value)) {
        return candidate;
      }
    }
    return null;
  }
}
