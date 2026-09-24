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
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * VAT-specific diagnostics, present on {@code vat} validations.
 *
 * <p>Two questions are answered separately. {@code formatValid} is decided locally and always
 * available. {@code registered} needs VIES and is <strong>nullable</strong>: {@code null} means the
 * registry could not be consulted — unknown, never "not registered". Treating {@code null} as
 * {@code false} rejects legitimate customers during someone else's outage.
 *
 * @param formatValid        the number matches its member state's structure
 * @param registered         present in the registry; {@code null} when VIES could not be consulted
 * @param countryCode        member state, e.g. {@code IE}; Greece is {@code EL}, Northern Ireland
 *                           {@code XI}
 * @param source             where the registration verdict came from
 * @param checkedAt          when the registration was last confirmed against VIES
 * @param traderName         registered name, when the member state discloses it
 * @param traderAddress      registered address, when the member state discloses it
 * @param viesAvailable      whether VIES could answer for this country during the request
 * @param consultationNumber the receipt VIES issues to an identified requester — present only when
 *                           your VerifNow account has its own VAT number configured
 * @param traderNameMatch    present when a trader name was sent: whether it belongs to the
 *                           registered holder. Since 2.9.0.
 * @param traderNameMatchSource who compared — {@code VERIFNOW} against the name VIES published, or
 *                           {@code VIES} itself (Spain). Since 2.9.0.
 * @since 2.2.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VatDetails(
    @JsonProperty("format_valid") boolean formatValid,
    @JsonProperty("registered") Boolean registered,
    @JsonProperty("country_code") String countryCode,
    @JsonProperty("source") VatSource source,
    @JsonProperty("checked_at") Instant checkedAt,
    @JsonProperty("trader_name") String traderName,
    @JsonProperty("trader_address") String traderAddress,
    @JsonProperty("vies_available") boolean viesAvailable,
    @JsonProperty("consultation_number") String consultationNumber,
    @JsonProperty("trader_name_match") TraderNameMatch traderNameMatch,
    @JsonProperty("trader_name_match_source") TraderNameMatchSource traderNameMatchSource
) {

  /** The 2.8.0 shape, kept so code that built one — tests, typically — still compiles. */
  public VatDetails(boolean formatValid, Boolean registered, String countryCode, VatSource source,
      Instant checkedAt, String traderName, String traderAddress, boolean viesAvailable,
      String consultationNumber) {
    this(formatValid, registered, countryCode, source, checkedAt, traderName, traderAddress,
        viesAvailable, consultationNumber, null, null);
  }
}
