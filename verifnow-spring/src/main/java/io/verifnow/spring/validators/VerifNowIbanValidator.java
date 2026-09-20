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

import io.verifnow.core.client.IbanDetails;
import io.verifnow.core.client.VerifNowClient;
import io.verifnow.core.client.ValidationResult;
import io.verifnow.spring.annotations.VerifNowIban;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class VerifNowIbanValidator implements ConstraintValidator<VerifNowIban, String> {
  private final VerifNowClient apiClient;
  private boolean allowNull = true;
  private boolean requireSepa = false;

  @Autowired
  public VerifNowIbanValidator(VerifNowClient apiClient) {
    this.apiClient = apiClient;
  }

  @Override
  public void initialize(VerifNowIban constraintAnnotation) {
    this.allowNull = constraintAnnotation.allowNull();
    this.requireSepa = constraintAnnotation.requireSepa();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) return allowNull;
    try {
      ValidationResult r = apiClient.validate("iban", value);
      if (r == null || !r.isValid()) return false;

      IbanDetails details = r.getIbanDetails();
      if (requireSepa && details != null && !details.sepa()) {
        return Violations.add(context, "{validation.verifnow.iban.sepa.required}",
            Map.of("countryCode", String.valueOf(details.countryCode())));
      }
      return true;
    } catch (Exception ex) {
      return false;
    }
  }
}
