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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/**
 * A VAT rate applying to part of a member state only — an overseas department, an autonomous
 * region, an island.
 *
 * @param rate      the rate, e.g. {@code 8.5}
 * @param note      where it applies, in the words of the Commission's TEDB
 * @param euVatArea {@code false} for the Canary Islands and the French overseas territories, which
 *                  the VAT Directive excludes (Article 6(1)): goods shipped there from another
 *                  member state are an export, not a distance sale at this rate. {@code null} from
 *                  an API older than 2.9.0. Since 2.9.0.
 * @since 2.8.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RegionalVatRate(BigDecimal rate, String note, Boolean euVatArea) {

  /** The 2.8.0 shape, kept so code that built one still compiles. */
  public RegionalVatRate(BigDecimal rate, String note) {
    this(rate, note, null);
  }
}
