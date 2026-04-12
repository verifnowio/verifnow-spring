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
package io.verifnow.spring.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import io.verifnow.core.client.Deliverability;
import io.verifnow.core.client.RiskLevel;
import io.verifnow.spring.validators.VerifNowEmailValidator;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = VerifNowEmailValidator.class)
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
public @interface VerifNowEmail {
  String message() default "{validation.verifnow.email.invalid}";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
  String profile() default "";
  boolean allowNull() default true;

  /**
   * Maximum acceptable risk score (0–100). Validation fails if the
   * API returns a risk score strictly greater than this value.
   * Default is {@code 100} (no restriction).
   */
  int maxRiskScore() default 100;

  /**
   * Maximum acceptable risk level. Validation fails if the API returns
   * a risk level strictly higher than this value.
   * Order: LOW &lt; MEDIUM &lt; HIGH.
   * Default is {@link RiskLevel#HIGH} (no restriction).
   */
  RiskLevel maxRiskLevel() default RiskLevel.HIGH;

  /**
   * Allowed deliverability statuses. If non-empty, validation fails when
   * the API returns a deliverability status not in this list.
   * Default is empty (no restriction — all statuses accepted).
   */
  Deliverability[] allowedDeliverabilities() default {};
}
