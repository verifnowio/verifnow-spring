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
import io.verifnow.spring.validators.VerifNowIbanValidator;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validates an IBAN against the SWIFT registry structure for its country and its mod-97 check
 * digits.
 *
 * <p>Set {@link #requireSepa()} on a field that will be direct-debited: an IBAN from outside the
 * SEPA schemes' geographical scope is a correct IBAN that no SEPA mandate can collect from, and a
 * checkout would rather say so than take the order.
 *
 * <pre>{@code
 * public record CheckoutForm(
 *     @VerifNowIban(requireSepa = true) String iban,
 *     @VerifNowVat(requireRegistered = true) String vatNumber) {
 * }
 * }</pre>
 */
@Documented
@Constraint(validatedBy = VerifNowIbanValidator.class)
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
public @interface VerifNowIban {
  String message() default "{validation.verifnow.iban.invalid}";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
  String profile() default "";
  boolean allowNull() default true;

  /**
   * Reject a valid IBAN whose country is outside the SEPA schemes' geographical scope.
   *
   * <p>Off by default: an IBAN outside SEPA is perfectly payable by transfer, and only a direct
   * debit needs the account to be reachable under a mandate.
   *
   * @since 2.7.0
   */
  boolean requireSepa() default false;
}
