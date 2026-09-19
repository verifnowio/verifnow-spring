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
 * Spanish NIF diagnostics, present on {@code nif} validations.
 *
 * @param type          {@code DNI}, {@code NIE}, {@code NIF_K}, {@code NIF_L}, {@code NIF_M} or
 *                      {@code ENTITY}; kept as a string so a type added to the API later does not
 *                      make the response unreadable
 * @param naturalPerson the number belongs to a person rather than a company or other entity
 * @param checksumValid the control character is correct
 * @param entityLetter  for an entity, the letter that encodes its legal form, e.g. {@code B}
 * @param entityType    for an entity, its legal form
 * @since 2.5.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NifDetails(
    @JsonProperty("type") String type,
    @JsonProperty("natural_person") boolean naturalPerson,
    @JsonProperty("checksum_valid") boolean checksumValid,
    @JsonProperty("entity_letter") String entityLetter,
    @JsonProperty("entity_type") String entityType
) {
}
