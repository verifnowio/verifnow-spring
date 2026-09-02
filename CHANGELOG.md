# Changelog
All notable changes to this project will be documented in this file.

The format is based on **[Keep a Changelog](https://keepachangelog.com/en/1.1.0/)**  
and this project follows **[Semantic Versioning](https://semver.org/)**.

---

## [2.1.1] - 2026-09-02
### Fixed
- **The default `baseUrl` pointed at a host that does not exist.** Releases up to 2.1.0 defaulted
  to `https://api.verifnow.com`, which has no DNS record; the correct origin is
  `https://api.verifnow.io`. Combined with `failOnError=false` (the default), every call failed to
  connect and was then reported as `valid=true`, so an application following the README validated
  nothing while appearing to work. Anyone on 2.1.0 or earlier who did not set `baseUrl` explicitly
  should upgrade and re-check data accepted since integrating.
- `validateAsync()` called `/api/v1/{rule}` instead of `/api/v1/validate/{rule}`, so every
  asynchronous validation returned 404. Both entry points now share one request builder, which is
  what let the two paths drift apart in the first place.

### Changed
- Falling open after an API failure is now logged at `WARN`, naming the configured `baseUrl` and
  the rule. The behaviour is unchanged — `failOnError=false` still accepts the value unverified —
  but an outage is no longer silent. Set `verifnow.api.failOnError=true` to reject instead.

### Added
- First test suite for the SDK: request path, API key and SDK identification headers, request
  body, per-rule routing, and both the fail-open and fail-closed branches. The two bugs above
  shipped because nothing exercised the wire contract.

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
