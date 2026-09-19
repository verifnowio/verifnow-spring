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
 * US SSN diagnostics, present on {@code ssn} validations.
 *
 * <p>An SSN has no check digit, and since 2011 its area number says nothing about a state. The one
 * thing worth reporting is whether the number is an ITIN.
 *
 * @param itin the number is an IRS ITIN, not an SSN — invalid as an SSN, but an acceptable taxpayer
 *             number where one is accepted
 * @since 2.6.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SsnDetails(@JsonProperty("itin") boolean itin) {
}
