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
package io.verifnow.spring.client;

import static org.assertj.core.api.Assertions.assertThat;

import io.verifnow.core.client.PhoneLineType;
import io.verifnow.core.client.ValidationResult;
import io.verifnow.core.client.VatSource;
import io.verifnow.spring.config.ValidationProperties;
import java.io.IOException;
import java.time.Instant;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

/** Response bodies as the API sends them, read through the real client. */
class ValidationResultMappingTest {

  private MockWebServer server;

  @BeforeEach
  void startServer() throws IOException {
    server = new MockWebServer();
    server.start();
  }

  @AfterEach
  void stopServer() throws IOException {
    server.shutdown();
  }

  private ValidationResult respond(String rule, String body) {
    server.enqueue(new MockResponse().setHeader("Content-Type", "application/json").setBody(body));
    ValidationProperties props = new ValidationProperties();
    props.setBaseUrl(server.url("/").toString());
    props.setApiKey("test-api-key");
    props.setFailOnError(true); // a mapping failure must surface, not fall open
    return new VerifNowWebClient(WebClient.builder(), props).validate(rule, "value");
  }

  @Test
  void mapsVatDetails() {
    ValidationResult result = respond("vat", """
        {"valid":true,"normalizedValue":"IE6388047V","validationLevel":"STANDARD",
         "vatDetails":{"format_valid":true,"registered":true,"country_code":"IE","source":"LIVE",
           "checked_at":"2026-09-08T02:21:25Z","trader_name":"GOOGLE IRELAND LIMITED",
           "vies_available":true,"consultation_number":"WAPIAAAAX8k1abcd"}}""");

    var vat = result.getVatDetails();
    assertThat(vat).isNotNull();
    assertThat(vat.formatValid()).isTrue();
    assertThat(vat.registered()).isTrue();
    assertThat(vat.countryCode()).isEqualTo("IE");
    assertThat(vat.source()).isEqualTo(VatSource.LIVE);
    assertThat(vat.checkedAt()).isEqualTo(Instant.parse("2026-09-08T02:21:25Z"));
    assertThat(vat.traderName()).isEqualTo("GOOGLE IRELAND LIMITED");
    assertThat(vat.viesAvailable()).isTrue();
    assertThat(vat.consultationNumber()).isEqualTo("WAPIAAAAX8k1abcd");
  }

  @Test
  void keepsAnUnknownRegistrationNullRatherThanFalse() {
    // The distinction the whole VAT design carries: null means VIES could not be asked.
    ValidationResult result = respond("vat", """
        {"valid":true,"vatDetails":{"format_valid":true,"registered":null,"country_code":"FR",
          "source":"UNVERIFIED","vies_available":false}}""");

    assertThat(result.getVatDetails().registered()).isNull();
    assertThat(result.getVatDetails().source()).isEqualTo(VatSource.UNVERIFIED);
  }

  @Test
  void anUnknownVatSourceDoesNotBreakTheResponse() {
    ValidationResult result = respond("vat", """
        {"valid":true,"vatDetails":{"format_valid":true,"registered":true,"source":"SOMETHING_NEW",
          "vies_available":true,"field_added_later":1}}""");

    assertThat(result.isValid()).isTrue();
    assertThat(result.getVatDetails().registered()).isTrue();
    assertThat(result.getVatDetails().source()).isNull();
  }

  @Test
  void mapsIbanDetailsKeepingStructureAndChecksumApart() {
    // Valid check digits on an impossible length: the case the old validator called valid.
    ValidationResult result = respond("iban", """
        {"valid":false,"message":"A FR IBAN is 27 characters long",
         "originalValue":"FR23111111111111111111111","validationLevel":"STANDARD",
         "ibanDetails":{"country_code":"FR","structure_valid":false,"checksum_valid":true,
           "length":25,"expected_length":27}}""");

    var iban = result.getIbanDetails();
    assertThat(result.isValid()).isFalse();
    assertThat(iban.checksumValid()).isTrue();
    assertThat(iban.structureValid()).isFalse();
    assertThat(iban.countryCode()).isEqualTo("FR");
    assertThat(iban.length()).isEqualTo(25);
    assertThat(iban.expectedLength()).isEqualTo(27);
    assertThat(iban.formatted()).isNull();
  }

  @Test
  void mapsNasDetails() {
    ValidationResult result = respond("nas", """
        {"valid":true,"normalizedValue":"046454286","originalValue":"046 454 286",
         "nasDetails":{"checksum_valid":true,"temporary_resident":false,
           "individual_series":false,"formatted":"046 454 286"}}""");

    var nas = result.getNasDetails();
    assertThat(nas.checksumValid()).isTrue();
    assertThat(nas.temporaryResident()).isFalse();
    assertThat(nas.individualSeries()).isFalse();
    assertThat(nas.formatted()).isEqualTo("046 454 286");
  }

  @Test
  void mapsPhoneDetails() {
    ValidationResult result = respond("phone", """
        {"valid":true,"normalizedValue":"+33612345678","originalValue":"+33 6 12 34 56 78",
         "phoneDetails":{"country_code":"FR","calling_code":33,"line_type":"MOBILE",
           "international_format":"+33 6 12 34 56 78","national_format":"06 12 34 56 78"}}""");

    var phone = result.getPhoneDetails();
    assertThat(result.getNormalizedValue()).isEqualTo("+33612345678");
    assertThat(phone.countryCode()).isEqualTo("FR");
    assertThat(phone.callingCode()).isEqualTo(33);
    assertThat(phone.lineType()).isEqualTo(PhoneLineType.MOBILE);
    assertThat(phone.internationalFormat()).isEqualTo("+33 6 12 34 56 78");
    assertThat(phone.nationalFormat()).isEqualTo("06 12 34 56 78");
  }

  @Test
  void anUnknownLineTypeReadsAsUnknown() {
    ValidationResult result = respond("phone", """
        {"valid":true,"phoneDetails":{"country_code":"FR","calling_code":33,"line_type":"SATELLITE"}}""");

    assertThat(result.getPhoneDetails().lineType()).isEqualTo(PhoneLineType.UNKNOWN);
  }

  @Test
  void freeProviderIsEmptyWhenThePlanDidNotComputeIt() {
    // FREE and STARTER: the API omits free_provider. It must not read as "not a free provider".
    ValidationResult standard = respond("email", """
        {"valid":true,"emailDetails":{"signals":{"syntax_valid":true,"mx_valid":true,
          "typo_detected":true,"suggested_domain":"gmail.com","disposable":false,
          "role_based":true},"risk_score":30,"applied_level":"STANDARD"}}""");

    var signals = standard.getEmailDetails().getSignals();
    assertThat(signals.freeProviderIfComputed()).isEmpty();
    assertThat(signals.isFreeProvider()).isFalse();
    assertThat(signals.getSuggestedDomain()).isEqualTo("gmail.com");
    assertThat(signals.isRoleBased()).isTrue();
    assertThat(signals.getDomainAgeDays()).isNull();
  }

  @Test
  void freeProviderIsPresentWhenComputed() {
    ValidationResult advanced = respond("email", """
        {"valid":true,"emailDetails":{"signals":{"syntax_valid":true,"mx_valid":true,
          "free_provider":false,"domain_age_days":11554},"applied_level":"ADVANCED"}}""");

    var signals = advanced.getEmailDetails().getSignals();
    assertThat(signals.freeProviderIfComputed()).contains(false);
    assertThat(signals.getDomainAgeDays()).isEqualTo(11554);
  }

  @Test
  void anExplicitNullFreeProviderStaysUncomputed() {
    ValidationResult result = respond("email", """
        {"valid":true,"emailDetails":{"signals":{"free_provider":null}}}""");

    assertThat(result.getEmailDetails().getSignals().freeProviderIfComputed()).isEmpty();
  }
}
