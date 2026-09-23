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
import java.util.List;

/**
 * VAT rates of every EU member state.
 *
 * @param source    always {@code TEDB}, the Commission's Taxes in Europe Database
 * @param sourceUrl where TEDB is published
 * @param rates     one entry per member state retrieved so far — normally all 27
 * @since 2.8.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VatRates(String source, String sourceUrl, List<CountryVatRates> rates) {
}
