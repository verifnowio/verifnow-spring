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
import io.verifnow.spring.validators.VerifNowVatValidator;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = VerifNowVatValidator.class)
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
public @interface VerifNowVat {
  String message() default "{validation.verifnow.vat.invalid}";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
  String profile() default "";
  boolean allowNull() default true;

  /**
   * Reject a number whose registration VIES could not confirm.
   *
   * <p>Off by default, and deliberately so. When VIES is unreachable for a member state — which
   * happens several times a month — the API reports the registration as unknown and still marks a
   * well-formed number valid. Turning this on rejects legitimate businesses during those outages;
   * do it only where an unconfirmed registration is genuinely unacceptable, and prefer re-checking
   * later when you can.
   *
   * @since 2.2.0
   */
  boolean requireRegistered() default false;
}
