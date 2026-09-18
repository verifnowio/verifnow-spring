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

public class ValidationResult {
  private boolean valid;
  private String normalizedValue;
  private String originalValue;
  private String message;
  private String validationLevel;
  private EmailDetails emailDetails;
  private VatDetails vatDetails;
  private PhoneDetails phoneDetails;
  private IbanDetails ibanDetails;
  private NasDetails nasDetails;

  public ValidationResult() {}

  public ValidationResult(boolean valid, String originalValue, String message) {
    this.valid = valid;
    this.originalValue = originalValue;
    this.message = message;
  }

  public boolean isValid() {
    return valid;
  }

  public void setValid(boolean valid) {
    this.valid = valid;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getNormalizedValue() {
    return normalizedValue;
  }

  public void setNormalizedValue(String normalizedValue) {
    this.normalizedValue = normalizedValue;
  }

  public String getOriginalValue() {
    return originalValue;
  }

  public void setOriginalValue(String originalValue) {
    this.originalValue = originalValue;
  }

  public String getValidationLevel() {
    return validationLevel;
  }

  public void setValidationLevel(String validationLevel) {
    this.validationLevel = validationLevel;
  }

  public EmailDetails getEmailDetails() {
    return emailDetails;
  }

  public void setEmailDetails(EmailDetails emailDetails) {
    this.emailDetails = emailDetails;
  }

  /**
   * VAT diagnostics, present on {@code vat} validations. Read {@link VatDetails#registered()}
   * rather than {@link #isValid()} when an unconfirmed registration must not be accepted.
   *
   * @since 2.2.0
   */
  public VatDetails getVatDetails() {
    return vatDetails;
  }

  public void setVatDetails(VatDetails vatDetails) {
    this.vatDetails = vatDetails;
  }

  /**
   * Phone diagnostics — country, line type and formats — present on {@code phone} validations.
   *
   * @since 2.2.0
   */
  public PhoneDetails getPhoneDetails() {
    return phoneDetails;
  }

  public void setPhoneDetails(PhoneDetails phoneDetails) {
    this.phoneDetails = phoneDetails;
  }

  /**
   * IBAN diagnostics, present on {@code iban} validations. Read {@link IbanDetails#structureValid()}
   * and {@link IbanDetails#checksumValid()} to tell an impossible account number from a typo.
   *
   * @since 2.3.0
   */
  public IbanDetails getIbanDetails() {
    return ibanDetails;
  }

  public void setIbanDetails(IbanDetails ibanDetails) {
    this.ibanDetails = ibanDetails;
  }

  /**
   * Canadian SIN diagnostics, present on {@code nas} validations.
   *
   * @since 2.4.0
   */
  public NasDetails getNasDetails() {
    return nasDetails;
  }

  public void setNasDetails(NasDetails nasDetails) {
    this.nasDetails = nasDetails;
  }
}
