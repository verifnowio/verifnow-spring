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

import io.verifnow.core.client.Deliverability;
import io.verifnow.core.client.EmailDetails;
import io.verifnow.core.client.RiskLevel;
import io.verifnow.core.client.VerifNowClient;
import io.verifnow.core.client.ValidationResult;
import io.verifnow.spring.annotations.VerifNowEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class VerifNowEmailValidator implements ConstraintValidator<VerifNowEmail, String> {
  private final VerifNowClient apiClient;
  private boolean allowNull = true;
  private int maxRiskScore = 100;
  private RiskLevel maxRiskLevel = RiskLevel.HIGH;
  private Set<Deliverability> allowedDeliverabilities = EnumSet.noneOf(Deliverability.class);

  @Autowired
  public VerifNowEmailValidator(VerifNowClient apiClient) {
    this.apiClient = apiClient;
  }

  @Override
  public void initialize(VerifNowEmail constraintAnnotation) {
    this.allowNull = constraintAnnotation.allowNull();
    this.maxRiskScore = constraintAnnotation.maxRiskScore();
    this.maxRiskLevel = constraintAnnotation.maxRiskLevel();

    Deliverability[] deliverabilities = constraintAnnotation.allowedDeliverabilities();
    if (deliverabilities.length > 0) {
      this.allowedDeliverabilities = EnumSet.copyOf(Arrays.asList(deliverabilities));
    }
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) return allowNull;
    try {
      ValidationResult r = apiClient.validate("email", value);
      if (r == null || !r.isValid()) return false;

      EmailDetails details = r.getEmailDetails();
      // No email details available — fall back to the basic valid flag
      if (details == null) return true;

      // Risk score check (0 = lowest risk, 100 = highest)
      if (details.getRiskScore() > maxRiskScore) {
        return addViolation(context,
            "{validation.verifnow.email.risk_score.exceeded}",
            Map.of("riskScore", details.getRiskScore(),
                   "maxRiskScore", maxRiskScore));
      }

      // Risk level check (ordinal comparison: LOW < MEDIUM < HIGH)
      if (details.getRiskLevel() != null
          && details.getRiskLevel().ordinal() > maxRiskLevel.ordinal()) {
        return addViolation(context,
            "{validation.verifnow.email.risk_level.exceeded}",
            Map.of("riskLevel", details.getRiskLevel().name(),
                   "maxRiskLevel", maxRiskLevel.name()));
      }

      // Deliverability check
      if (!allowedDeliverabilities.isEmpty()
          && details.getDeliverability() != null
          && !allowedDeliverabilities.contains(details.getDeliverability())) {
        String allowed = allowedDeliverabilities.stream()
            .map(Deliverability::name)
            .collect(Collectors.joining(", "));
        return addViolation(context,
            "{validation.verifnow.email.deliverability.rejected}",
            Map.of("deliverability", details.getDeliverability().name(),
                   "allowedDeliverabilities", allowed));
      }

      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  /**
   * Replaces the default violation with a specific message template and
   * injects runtime parameters via Hibernate Validator's extended API.
   * Falls back to the standard Jakarta API (template only, no parameters)
   * if Hibernate Validator is not on the classpath.
   *
   * @return always {@code false} — for use as {@code return addViolation(…);}
   */
  private boolean addViolation(ConstraintValidatorContext context,
                               String template, Map<String, Object> params) {
    context.disableDefaultConstraintViolation();
    try {
      HibernateConstraintValidatorContext hvContext =
          context.unwrap(HibernateConstraintValidatorContext.class);
      params.forEach(hvContext::addMessageParameter);
      hvContext.buildConstraintViolationWithTemplate(template)
          .addConstraintViolation();
    } catch (Exception e) {
      // Hibernate Validator not available — use template without parameters
      context.buildConstraintViolationWithTemplate(template)
          .addConstraintViolation();
    }
    return false;
  }
}
