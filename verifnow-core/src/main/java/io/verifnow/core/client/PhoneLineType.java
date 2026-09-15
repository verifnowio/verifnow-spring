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
 * Kind of line a phone number belongs to, according to its country's numbering plan.
 *
 * <p>A {@link #PREMIUM_RATE} or {@link #VOIP} number is still valid — it exists. This is how a
 * caller decides to exclude one.
 *
 * @since 2.2.0
 */
public enum PhoneLineType {
  MOBILE,
  FIXED_LINE,
  /** The numbering plan does not distinguish the two — the case for the US and Canada. */
  FIXED_LINE_OR_MOBILE,
  TOLL_FREE,
  PREMIUM_RATE,
  SHARED_COST,
  VOIP,
  PERSONAL_NUMBER,
  PAGER,
  UAN,
  VOICEMAIL,
  UNKNOWN;

  /** Reads the API value; a type this SDK version does not know maps to {@link #UNKNOWN}. */
  @JsonCreator
  public static PhoneLineType fromValue(String value) {
    if (value == null) {
      return null;
    }
    for (PhoneLineType type : values()) {
      if (type.name().equals(value)) {
        return type;
      }
    }
    return UNKNOWN;
  }
}
