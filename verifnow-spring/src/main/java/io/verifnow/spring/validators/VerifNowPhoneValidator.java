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
import io.verifnow.core.client.PhoneDetails;
import io.verifnow.core.client.PhoneLineType;
import io.verifnow.core.client.ValidationResult;
import io.verifnow.spring.annotations.VerifNowPhone;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class VerifNowPhoneValidator implements ConstraintValidator<VerifNowPhone, String> {
  private final VerifNowClient apiClient;
  private boolean allowNull = true;
  private Set<PhoneLineType> rejectedLineTypes = EnumSet.noneOf(PhoneLineType.class);

  @Autowired
  public VerifNowPhoneValidator(VerifNowClient apiClient) {
    this.apiClient = apiClient;
  }

  @Override
  public void initialize(VerifNowPhone constraintAnnotation) {
    this.allowNull = constraintAnnotation.allowNull();
    PhoneLineType[] rejected = constraintAnnotation.rejectedLineTypes();
    if (rejected.length > 0) {
      this.rejectedLineTypes = EnumSet.copyOf(Arrays.asList(rejected));
    }
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) return allowNull;
    try {
      ValidationResult r = apiClient.validate("phone", value);
      if (r == null || !r.isValid()) return false;

      PhoneDetails details = r.getPhoneDetails();
      if (details != null && details.lineType() != null
          && rejectedLineTypes.contains(details.lineType())) {
        return Violations.add(context, "{validation.verifnow.phone.line_type.rejected}",
            Map.of("lineType", details.lineType().name()));
      }
      return true;
    } catch (Exception ex) {
      return false;
    }
  }
}
