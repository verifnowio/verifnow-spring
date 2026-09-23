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
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * One EU member state's VAT rates, from the European Commission's TEDB.
 *
 * <p>These are the rates the member state has, not the rate an invoice carries. Which one applies
 * depends on who sells to whom and what: in B2B trade between member states the invoice is usually
 * zero-rated under the reverse charge, whatever the buyer's country rate is.
 *
 * @param countryCode   member state as TEDB and VIES name it: Greece is {@code EL}
 * @param standardRate  the national standard rate, e.g. {@code 20} for France
 * @param reducedRates  every reduced, super-reduced and parking rate on the mainland territory,
 *                      ascending; TEDB's own sub-labels disagree between member states and are
 *                      not reproduced
 * @param regionalRates rates for part of the territory only
 * @param situationOn   the date TEDB says these rates apply from
 * @param fetchedAt     when VerifNow last retrieved them from TEDB
 * @since 2.8.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CountryVatRates(
    String countryCode,
    BigDecimal standardRate,
    List<BigDecimal> reducedRates,
    List<RegionalVatRate> regionalRates,
    LocalDate situationOn,
    Instant fetchedAt
) {
}
