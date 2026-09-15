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
package io.verifnow.spring.validators;

import io.verifnow.core.client.VerifNowClient;
import io.verifnow.core.client.ValidationResult;
import io.verifnow.core.client.VatDetails;
import io.verifnow.spring.annotations.VerifNowVat;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class VerifNowVatValidator implements ConstraintValidator<VerifNowVat, String> {

  private final VerifNowClient apiClient;
  private boolean allowNull = true;
  private boolean requireRegistered = false;

  @Autowired
  public VerifNowVatValidator(VerifNowClient apiClient) {
    this.apiClient = apiClient;
  }

  @Override
  public void initialize(VerifNowVat constraintAnnotation) {
    this.allowNull = constraintAnnotation.allowNull();
    this.requireRegistered = constraintAnnotation.requireRegistered();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) return allowNull;
    try {
      ValidationResult r = apiClient.validate("vat", value);
      if (r == null || !r.isValid()) return false;

      if (requireRegistered) {
        VatDetails details = r.getVatDetails();
        if (details == null || !Boolean.TRUE.equals(details.registered())) {
          String source = details == null || details.source() == null
              ? "UNKNOWN" : details.source().name();
          return Violations.add(context,
              "{validation.verifnow.vat.registration.unconfirmed}", Map.of("source", source));
        }
      }
      return true;
    } catch (Exception ex) {
      return false;
    }
  }
}
