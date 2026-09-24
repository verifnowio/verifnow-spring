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
 * Who compared the names.
 *
 * @since 2.9.0
 */
public enum TraderNameMatchSource {

  /** VIES compared the name against the national register itself (Spain). */
  VIES,

  /** VerifNow compared the name with the one VIES published, ignoring case, accents, punctuation and common legal forms. */
  VERIFNOW;

  /**
   * Reads the API value, returning {@code null} for one this SDK version does not know, so a value
   * added to the API later cannot make the whole response unreadable.
   */
  @JsonCreator
  public static TraderNameMatchSource fromValue(String value) {
    if (value == null) {
      return null;
    }
    for (TraderNameMatchSource candidate : values()) {
      if (candidate.name().equals(value)) {
        return candidate;
      }
    }
    return null;
  }
}
