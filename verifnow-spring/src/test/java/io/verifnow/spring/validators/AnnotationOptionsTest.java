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

import static org.assertj.core.api.Assertions.assertThat;

import io.verifnow.core.client.EmailDetails;
import io.verifnow.core.client.EmailSignals;
import io.verifnow.core.client.PhoneDetails;
import io.verifnow.core.client.PhoneLineType;
import io.verifnow.core.client.ValidationResult;
import io.verifnow.core.client.VatDetails;
import io.verifnow.core.client.VatSource;
import io.verifnow.core.client.VerifNowClient;
import io.verifnow.spring.annotations.VerifNowEmail;
import io.verifnow.spring.annotations.VerifNowPhone;
import io.verifnow.spring.annotations.VerifNowVat;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** The 2.2.0 annotation options, run through Hibernate Validator with a canned API answer. */
class AnnotationOptionsTest {

  private static Validator validatorAnswering(ValidationResult answer) {
    VerifNowClient client = new VerifNowClient() {
      @Override
      public ValidationResult validate(String rule, String value) {
        return answer;
      }

      @Override
      public CompletableFuture<ValidationResult> validateAsync(String rule, String value) {
        return CompletableFuture.completedFuture(answer);
      }
    };
    ConstraintValidatorFactory factory = new ConstraintValidatorFactory() {
      @Override
      @SuppressWarnings("unchecked")
      public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
        if (key == VerifNowVatValidator.class) return (T) new VerifNowVatValidator(client);
        if (key == VerifNowPhoneValidator.class) return (T) new VerifNowPhoneValidator(client);
        if (key == VerifNowEmailValidator.class) return (T) new VerifNowEmailValidator(client);
        throw new IllegalArgumentException(key.getName());
      }

      @Override
      public void releaseInstance(ConstraintValidator<?, ?> instance) {
      }
    };
    return Validation.byDefaultProvider().configure()
        .constraintValidatorFactory(factory)
        // No EL implementation in the test classpath; parameters are all these messages use.
        .messageInterpolator(new ParameterMessageInterpolator())
        .buildValidatorFactory()
        .getValidator();
  }

  private static <T> Set<String> messages(Set<ConstraintViolation<T>> violations) {
    return violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
  }

  private static ValidationResult vat(Boolean registered, VatSource source) {
    ValidationResult r = new ValidationResult(true, "FR12345678901", "ok");
    r.setVatDetails(new VatDetails(true, registered, "FR", source, null, null, null,
        registered != null, null));
    return r;
  }

  record DefaultVat(@VerifNowVat String vat) {}

  record StrictVat(@VerifNowVat(requireRegistered = true) String vat) {}

  @Test
  void vat_unknownRegistrationIsAcceptedByDefault() {
    // VIES down for that member state. A default that rejects here loses real customers.
    Validator validator = validatorAnswering(vat(null, VatSource.UNVERIFIED));

    assertThat(validator.validate(new DefaultVat("FR12345678901"))).isEmpty();
  }

  @Test
  void vat_requireRegisteredRejectsAnUnknownRegistrationAndSaysWhy() {
    Validator validator = validatorAnswering(vat(null, VatSource.UNVERIFIED));

    assertThat(messages(validator.validate(new StrictVat("FR12345678901"))))
        .singleElement().asString()
        .contains("could not be confirmed")
        .contains("UNVERIFIED");
  }

  @Test
  void vat_requireRegisteredAcceptsAConfirmedRegistration() {
    Validator validator = validatorAnswering(vat(true, VatSource.STALE));

    assertThat(validator.validate(new StrictVat("FR12345678901"))).isEmpty();
  }

  private static ValidationResult phone(PhoneLineType type) {
    ValidationResult r = new ValidationResult(true, "+33899123456", "Valid phone number");
    r.setPhoneDetails(new PhoneDetails("FR", 33, type, null, null));
    return r;
  }

  record AnyPhone(@VerifNowPhone String phone) {}

  record NoPremiumPhone(
      @VerifNowPhone(rejectedLineTypes = PhoneLineType.PREMIUM_RATE) String phone) {}

  @Test
  void phone_everyValidLineTypeIsAcceptedByDefault() {
    Validator validator = validatorAnswering(phone(PhoneLineType.PREMIUM_RATE));

    assertThat(validator.validate(new AnyPhone("+33 8 99 12 34 56"))).isEmpty();
  }

  @Test
  void phone_rejectedLineTypeProducesItsOwnMessage() {
    Validator validator = validatorAnswering(phone(PhoneLineType.PREMIUM_RATE));

    assertThat(messages(validator.validate(new NoPremiumPhone("+33 8 99 12 34 56"))))
        .containsExactly("Phone numbers of type PREMIUM_RATE are not accepted");
  }

  @Test
  void phone_otherLineTypesPassTheFilter() {
    Validator validator = validatorAnswering(phone(PhoneLineType.VOIP));

    assertThat(validator.validate(new NoPremiumPhone("+33 9 70 12 34 56"))).isEmpty();
  }

  private static ValidationResult email(boolean disposable, boolean roleBased) {
    EmailSignals signals = new EmailSignals();
    signals.setSyntaxValid(true);
    signals.setMxValid(true);
    signals.setDisposable(disposable);
    signals.setRoleBased(roleBased);
    EmailDetails details = new EmailDetails();
    details.setSignals(signals);
    details.setRiskScore(30);
    ValidationResult r = new ValidationResult(true, "contact@acme.fr", "ok");
    r.setEmailDetails(details);
    return r;
  }

  record AnyEmail(@VerifNowEmail String email) {}

  record NoDisposable(@VerifNowEmail(rejectDisposable = true) String email) {}

  record NoRole(@VerifNowEmail(rejectRoleBased = true) String email) {}

  @Test
  void email_roleAddressIsAcceptedByDefault() {
    Validator validator = validatorAnswering(email(false, true));

    assertThat(validator.validate(new AnyEmail("contact@acme.fr"))).isEmpty();
  }

  @Test
  void email_rejectRoleBased() {
    Validator validator = validatorAnswering(email(false, true));

    assertThat(messages(validator.validate(new NoRole("contact@acme.fr"))))
        .singleElement().asString().contains("shared mailbox");
  }

  @Test
  void email_rejectDisposable() {
    Validator validator = validatorAnswering(email(true, false));

    assertThat(messages(validator.validate(new NoDisposable("x@mailinator.com"))))
        .singleElement().asString().contains("disposable");
    assertThat(validator.validate(new NoRole("x@mailinator.com"))).isEmpty();
  }
}
