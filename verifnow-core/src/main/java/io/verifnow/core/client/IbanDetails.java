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

/**
 * IBAN-specific diagnostics, present on {@code iban} validations.
 *
 * <p>Structure and checksum are reported separately because they fail for different reasons.
 * {@code structureValid} answers "could this be an account number in that country" — the SWIFT
 * registry's length and character layout. {@code checksumValid} answers "was it typed correctly" —
 * the mod-97 check digits. Valid check digits on an impossible length is a real case, and the one
 * a payment form must not accept.
 *
 * <p>No bank name or BIC: that needs a bank registry the API does not hold.
 *
 * @param countryCode    the IBAN's country, from its first two characters
 * @param structureValid length and character layout match the registry entry for that country
 * @param checksumValid  the mod-97 check digits are correct
 * @param length         length of the value as submitted, spaces removed
 * @param expectedLength length the registry requires; null for a country outside the registry
 * @param formatted      print format in groups of four; present only for a valid IBAN
 * @since 2.3.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record IbanDetails(
    @JsonProperty("country_code") String countryCode,
    @JsonProperty("structure_valid") boolean structureValid,
    @JsonProperty("checksum_valid") boolean checksumValid,
    @JsonProperty("length") int length,
    @JsonProperty("expected_length") Integer expectedLength,
    @JsonProperty("formatted") String formatted
) {
}
