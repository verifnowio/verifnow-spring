# Changelog
All notable changes to this project will be documented in this file.

The format is based on **[Keep a Changelog](https://keepachangelog.com/en/1.1.0/)**  
and this project follows **[Semantic Versioning](https://semver.org/)**.

---

## [2.1.0] - 2026-04-11
### Added
- Extended API response model with `emailDetails` support: `EmailDetails`, `EmailSignals`, `RiskLevel` and `Deliverability` types in `verifnow-core`.
- New `validationLevel` and `emailDetails` fields on `ValidationResult`.
- Configurable email validation via `@VerifNowEmail` annotation:
  - `maxRiskScore` — reject emails above a given risk score (0–100).
  - `maxRiskLevel` — reject emails above a given risk level (`LOW`, `MEDIUM`, `HIGH`).
  - `allowedDeliverabilities` — restrict accepted deliverability statuses (`DELIVERABLE`, `RISKY`, `UNDELIVERABLE`, `UNKNOWN`).
- All new attributes default to permissive values, preserving full backward compatibility with existing `@VerifNowEmail` usage.
- Contextual validation error messages with dynamic parameters for each failure reason (`risk_score.exceeded`, `risk_level.exceeded`, `deliverability.rejected`).
- Bundled `ContributorValidationMessages.properties` so Hibernate Validator resolves human-readable messages out of the box, overridable by the consuming application.

### Fixed
- Validation messages were not interpolated (`messages.properties` was ignored by Jakarta Bean Validation). Replaced with the standard `ContributorValidationMessages.properties` mechanism.
- `src/main/resources` was excluded from the jar due to a `<resources>` override in `verifnow-spring/pom.xml`.

---

## [2.0.1] - 2025-11-20
### Added
- SDK version header in API requests for better traceability and monitoring.

---

## [2.0.0] - 2025-11-16
### Added
- First stable release of the SDK.
- Lightweight Java client based on WebClient (Java 21+).
- Centralized error handling.
- Initial documentation

---
