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
 * Where a VAT registration verdict came from.
 *
 * <p>VIES publishes no SLA and drops member states several times a month, so a VAT answer is not
 * always a live one. Branch on this rather than on {@link ValidationResult#isValid()} whenever the
 * difference matters for your own compliance.
 *
 * @since 2.2.0
 */
public enum VatSource {
  /** Confirmed against VIES during this request. */
  LIVE,
  /** Served from a VIES answer less than 24 hours old. */
  CACHE,
  /** VIES was unreachable, so an older cached answer was used. */
  STALE,
  /** VIES was unreachable and nothing was cached. Registration is unknown. */
  UNVERIFIED,
  /** The country is outside VIES, so no registry lookup is possible. */
  NOT_APPLICABLE;

  /**
   * Reads the API value, returning {@code null} for one this SDK version does not know. A source
   * added to the API must not make the whole response unreadable — which, with fail-open enabled,
   * would silently turn every VAT answer into an unverified acceptance.
   */
  @JsonCreator
  public static VatSource fromValue(String value) {
    if (value == null) {
      return null;
    }
    for (VatSource source : values()) {
      if (source.name().equals(value)) {
        return source;
      }
    }
    return null;
  }
}
