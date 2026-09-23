# VerifNow Java SDK

[![Build](https://github.com/verifnowio/verifnow-spring/actions/workflows/ci.yml/badge.svg)](https://github.com/verifnowio/verifnow-spring/actions/workflows/ci.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.verifnow/verifnow-parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.verifnow/verifnow-parent)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

Official Java SDK for the **VerifNow** API — data validation, identity verification, and compliance services.

## Before you start

This SDK integrates with the VerifNow API. To use it:

1. Create an account and subscribe to a plan.
2. Create one or more API keys (apiKey) in the client app.
3. Configure this API key in your application so the SDK can call VerifNow.

Website and documentation:
- Official site with overview, usage guide, documentation, and link to the client app: https://www.verifnow.io

---

## Configuration (Spring Boot)

This starter auto-configures a Spring WebClient and exposes validation annotations. Properties are configured under the `verifnow.api` prefix.

Example with `application.yml`:

```yaml
verifnow:
  api:
    apiKey: "YOUR_API_KEY"           # required to call the VerifNow API
    # baseUrl: https://api.verifnow.io  # default
    # timeoutMs: 1500                  # default
    # cacheEnabled: true               # default
    # cacheTtlSeconds: 60              # default
    # failOnError: false               # default (fail-open)
```

Or with `application.properties`:

```properties
verifnow.api.apiKey=YOUR_API_KEY
# verifnow.api.baseUrl=https://api.verifnow.io
# verifnow.api.timeoutMs=1500
# verifnow.api.cacheEnabled=true
# verifnow.api.cacheTtlSeconds=60
# verifnow.api.failOnError=false
```

Notes:
- `apiKey` is required. Without a valid key, API calls will fail.
- `failOnError=false` is fail-open by default if the API/network errors out. Set to `true` to strictly fail validation on errors.
- `baseUrl` defaults to `https://api.verifnow.io`.

## Modules

- **verifnow-core**: Core validation functionality and API response models
- **verifnow-spring**: Spring Framework integration with Jakarta Bean Validation annotations
- **verifnow-spring-boot-starter**: Spring Boot auto-configuration starter


---
## 🚀 Quick Start

### Maven

```xml
<dependency>
    <groupId>io.verifnow</groupId>
    <artifactId>verifnow-spring-boot-starter</artifactId>
    <version>2.8.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'io.verifnow:verifnow-spring-boot-starter:2.8.0'
```

---
## Usage

### Basic validation

```java
public class User {
    @VerifNowEmail
    private String email;
    
    @VerifNowPhone
    private String phone;
    
    // getters and setters
}
```

Available annotations: `@VerifNowEmail`, `@VerifNowPhone`, `@VerifNowIban`, `@VerifNowVat`, `@VerifNowSsn`, `@VerifNowNas`, `@VerifNowNif`.

### Advanced email validation

Since **2.1.0**, `@VerifNowEmail` supports fine-grained control over email acceptance criteria using data returned by the VerifNow API (risk score, risk level, and deliverability status).

All attributes are optional and default to permissive values, so `@VerifNowEmail` without parameters behaves exactly as before.

```java
import io.verifnow.core.client.Deliverability;
import io.verifnow.core.client.RiskLevel;
import io.verifnow.spring.annotations.VerifNowEmail;

public class StrictUser {

    // Reject emails with a risk score above 30 (0–100 scale, 0 = lowest risk)
    @VerifNowEmail(maxRiskScore = 30)
    private String email;

    // Accept only LOW and MEDIUM risk levels (reject HIGH)
    @VerifNowEmail(maxRiskLevel = RiskLevel.MEDIUM)
    private String safeEmail;

    // Only allow emails confirmed as deliverable
    @VerifNowEmail(allowedDeliverabilities = { Deliverability.DELIVERABLE })
    private String deliverableEmail;

    // Combine multiple criteria
    @VerifNowEmail(
        maxRiskScore = 50,
        maxRiskLevel = RiskLevel.MEDIUM,
        allowedDeliverabilities = { Deliverability.DELIVERABLE, Deliverability.RISKY }
    )
    private String businessEmail;
}
```

| Attribute | Type | Default | Description |
|---|---|---|---|
| `maxRiskScore` | `int` | `100` | Maximum acceptable risk score (0–100). Validation fails if the score exceeds this value. |
| `maxRiskLevel` | `RiskLevel` | `HIGH` | Maximum acceptable risk level. Order: `LOW` < `MEDIUM` < `HIGH`. |
| `allowedDeliverabilities` | `Deliverability[]` | `{}` (all) | If non-empty, only the listed statuses are accepted: `DELIVERABLE`, `RISKY`, `UNDELIVERABLE`, `UNKNOWN`. |

### VAT, phone and email options (2.2.0)

All three default to accepting, so existing annotations behave as before.

```java
import io.verifnow.core.client.PhoneLineType;

public class B2bSignup {

    // Accepted by default when VIES is down for that member state: the number is well formed
    // and its registration is unknown, not absent. requireRegistered rejects that case too.
    @VerifNowVat
    private String vatNumber;

    // Every valid number passes unless its line type is listed. Numbers need a country code.
    @VerifNowPhone(rejectedLineTypes = { PhoneLineType.PREMIUM_RATE })
    private String phone;

    // contact@ and info@ are accepted unless rejectRoleBased = true.
    @VerifNowEmail(rejectDisposable = true)
    private String email;

    // Since 2.7.0: a correct Egyptian IBAN is valid, and cannot be direct-debited. Only a field
    // you will collect from needs this; a transfer works from anywhere.
    @VerifNowIban(requireSepa = true)
    private String iban;
}
```

| Annotation | Attribute | Default | Effect |
|---|---|---|---|
| `@VerifNowVat` | `requireRegistered` | `false` | Also reject when VIES could not confirm the registration. Rejects real businesses during VIES outages — use sparingly. |
| `@VerifNowPhone` | `rejectedLineTypes` | `{}` | Reject valid numbers of these types, e.g. `PREMIUM_RATE`. |
| `@VerifNowEmail` | `rejectDisposable` | `false` | Reject disposable mailbox providers. |
| `@VerifNowEmail` | `rejectRoleBased` | `false` | Reject shared mailboxes such as `info@`, `contact@`. |
| `@VerifNowIban` | `requireSepa` | `false` | Reject a valid IBAN from outside the SEPA area, which no direct debit mandate can collect from. Leave off for a field paid by transfer. *(2.7.0)* |

### Reading the diagnostics

`ValidationResult` carries the details behind each verdict:

```java
ValidationResult vat = client.validate("vat", "IE6388047V");
VatDetails details = vat.getVatDetails();
Boolean registered = details.registered();   // null = VIES could not be asked, never "no"
VatSource source = details.source();          // LIVE, CACHE, STALE, UNVERIFIED, NOT_APPLICABLE
String receipt = details.consultationNumber(); // when your account has a VAT number configured

ValidationResult phone = client.validate("phone", "+33 6 12 34 56 78");
phone.getNormalizedValue();                    // "+33612345678" (E.164)
phone.getPhoneDetails().lineType();            // MOBILE

EmailSignals signals = client.validate("email", "someone@gmail.com").getEmailDetails().getSignals();
signals.freeProviderIfComputed();              // Optional.empty() on FREE and STARTER
```

### VAT rates (2.8.0)

The rates of the 27 member states, retrieved daily from the Commission's
[TEDB](https://ec.europa.eu/taxation_customs/tedb/). Public reference data: these calls spend no
quota.

```java
CountryVatRates france = client.vatRate("FR");   // "GR" is accepted for Greece (EL)
france.standardRate();     // 20
france.reducedRates();     // [2.1, 5.5, 10] — which one applies depends on the product
france.regionalRates();    // [RegionalVatRate[rate=8.5, note=The standard VAT rate in Martinique…], …]
france.situationOn();      // 2026-07-01 — the date TEDB says these rates apply from

VatRates all = client.vatRates();                // all.rates(): one entry per member state
```

**These are the rates a member state has, not the rate an invoice carries.** In B2B trade between
member states the invoice is usually zero-rated under the reverse charge, whatever the buyer's
country rate is.

Unlike `validate`, the rate calls **never fail open**, whatever `failOnError` says: accepting a
value unverified is a policy you can choose, but returning a rate that was not read would be an
invented one. A country outside the union throws `WebClientResponseException.NotFound`.

`vatRates()` and `vatRate()` are `default` methods on `VerifNowClient`, so a class of your own
that implements the interface still compiles; it throws `UnsupportedOperationException` if called.

### Error messages

Each failure reason produces a specific, human-readable error message with contextual parameters:

| Failure | Message example |
|---|---|
| API says invalid | `Invalid email address` |
| Risk score exceeded | `Email rejected: risk score 75 exceeds the maximum allowed (30)` |
| Risk level exceeded | `Email rejected: risk level HIGH exceeds the maximum allowed (MEDIUM)` |
| Deliverability rejected | `Email rejected: deliverability RISKY is not in the allowed list (DELIVERABLE)` |

Default messages are bundled with the library and resolved automatically by Hibernate Validator.

To override them globally, define the keys in your application's `ValidationMessages.properties`:

```properties
validation.verifnow.email.invalid=Custom invalid email message
validation.verifnow.email.risk_score.exceeded=Risk score {riskScore} is too high (max: {maxRiskScore})
validation.verifnow.email.risk_level.exceeded=Risk level {riskLevel} is not acceptable (max: {maxRiskLevel})
validation.verifnow.email.deliverability.rejected=Deliverability {deliverability} not allowed (accepted: {allowedDeliverabilities})
```

You can also override per-annotation using the `message` attribute:

```java
@VerifNowEmail(maxRiskScore = 30, message = "This email does not meet our quality standards")
private String email;
```

---

## Quick try

A runnable example (minimal Spring Boot app and test) is available in the public repository `verifnowio/validation-java-example`: [https://github.com/verifnowio/validation-java-example](https://github.com/verifnowio/validation-java-example).

Try the integration in a few minutes inside an existing Spring Boot app:

1) Add the dependency (see Quick Start above).

2) Provide your API key (either config file or environment variable). With an environment variable:

```bash
export VERIFNOW_API_APIKEY="YOUR_API_KEY"
```

3) Add a minimal JUnit test to validate with the annotation:

```java
import io.verifnow.spring.annotations.VerifNowEmail;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class VerifNowQuickTryTest {
  static class Dto {
    @VerifNowEmail String email;
    Dto(String email) { this.email = email; }
  }

  @Autowired Validator validator;

  @Test
  void validatesEmail() {
    Set<ConstraintViolation<Dto>> violations = validator.validate(new Dto("john.doe@example.com"));
    assertTrue(violations.isEmpty(), () -> "Violations: " + violations);
  }
}
```

Then run tests:

```bash
mvn -q -Dtest=VerifNowQuickTryTest test
```


---

## Building from Source

### Prerequisites

- Java 21 or later
- Maven 3.6 or later

### Build

```bash
mvn clean install
```

### Run Tests

```bash
mvn clean verify
```

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Support

If you have any questions or issues, please open an issue on GitHub.
