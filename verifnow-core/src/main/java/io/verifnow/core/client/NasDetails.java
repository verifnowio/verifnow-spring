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
 * Canadian Social Insurance Number diagnostics, present on {@code nas} validations.
 *
 * <p>No province and no expiry date: the first digit no longer reliably identifies a province, and a
 * temporary resident's SIN expires with their permit, which only the document shows.
 *
 * @param checksumValid     the Luhn check digit is correct
 * @param temporaryResident a 9-series number, issued to temporary residents; it expires with the
 *                          holder's permit and the number does not say when
 * @param individualSeries  the first digit belongs to a series issued to individuals; false for
 *                          numbers starting with 0 or 8, including the sample number 046 454 286
 * @param formatted         printed form, e.g. {@code 046 454 286}
 * @since 2.4.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NasDetails(
    @JsonProperty("checksum_valid") boolean checksumValid,
    @JsonProperty("temporary_resident") boolean temporaryResident,
    @JsonProperty("individual_series") boolean individualSeries,
    @JsonProperty("formatted") String formatted
) {
}
