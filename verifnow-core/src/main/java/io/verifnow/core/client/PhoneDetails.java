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
 * Phone-specific diagnostics, present whenever the input parsed as an international number —
 * including when it is invalid for its country. The E.164 form of a valid number is
 * {@link ValidationResult#getNormalizedValue()}.
 *
 * @param countryCode         ISO 3166-1 alpha-2 country, e.g. {@code FR}; absent when the calling
 *                            code is shared by several countries
 * @param callingCode         international calling code without the plus sign, e.g. {@code 33}
 * @param lineType            kind of line; absent when the number is invalid
 * @param internationalFormat e.g. {@code +33 6 12 34 56 78}; absent when the number is invalid
 * @param nationalFormat      e.g. {@code 06 12 34 56 78}; absent when the number is invalid
 * @since 2.2.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PhoneDetails(
    @JsonProperty("country_code") String countryCode,
    @JsonProperty("calling_code") Integer callingCode,
    @JsonProperty("line_type") PhoneLineType lineType,
    @JsonProperty("international_format") String internationalFormat,
    @JsonProperty("national_format") String nationalFormat
) {
}
