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

import java.util.concurrent.CompletableFuture;

public interface VerifNowClient {

  ValidationResult validate(String rule, String value);

  CompletableFuture<ValidationResult> validateAsync(String rule, String value);

  /**
   * Validates a VAT number and checks whether it belongs to the named company.
   *
   * <p>{@link VatDetails#traderNameMatch()} answers {@code MATCH}, {@code MISMATCH} or
   * {@code NOT_AVAILABLE}. Where VIES publishes the holder's name, VerifNow compares; Spain has VIES
   * check a name it does not publish; Germany does neither.
   *
   * <p>A default method so that adding it breaks no existing implementation of this interface.
   *
   * @param value      the VAT number, with its country prefix
   * @param traderName the company you expect to hold it; at most 200 characters
   * @since 2.9.0
   */
  default ValidationResult validateVat(String value, String traderName) {
    throw new UnsupportedOperationException(getClass().getName() + " does not check trader names");
  }

  /**
   * EU VAT rates of every member state, from the European Commission's TEDB.
   *
   * <p>Public reference data: the call spends no quota. These are the rates a member state has, not
   * the rate an invoice carries — see {@link CountryVatRates}.
   *
   * <p>A default method so that adding it breaks no existing implementation of this interface; the
   * SDK's own client overrides it.
   *
   * @since 2.8.0
   */
  default VatRates vatRates() {
    throw new UnsupportedOperationException(getClass().getName() + " does not read VAT rates");
  }

  /**
   * One EU member state's VAT rates. Accepts {@code GR} for Greece as well as {@code EL}.
   *
   * @param countryCode a member state code, e.g. {@code FR}
   * @since 2.8.0
   */
  default CountryVatRates vatRate(String countryCode) {
    throw new UnsupportedOperationException(getClass().getName() + " does not read VAT rates");
  }

}
