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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.verifnow.core.client.CountryVatRates;
import io.verifnow.core.client.RegionalVatRate;
import io.verifnow.core.client.TraderNameMatch;
import io.verifnow.core.client.TraderNameMatchSource;
import io.verifnow.core.client.ValidationResult;
import io.verifnow.core.client.VatRates;
import io.verifnow.core.client.VerifNowClient;
import io.verifnow.spring.config.ValidationProperties;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

class VerifNowWebClientTest {

  private static final String VALID_BODY = """
      {"valid":true,"message":"Valid email address","normalizedValue":"user@example.com",\
      "originalValue":"USER@Example.Com","validationLevel":"STANDARD"}""";

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

  private VerifNowWebClient clientFor(ValidationProperties props) {
    return new VerifNowWebClient(WebClient.builder(), props);
  }

  private ValidationProperties propsPointingAtServer() {
    ValidationProperties props = new ValidationProperties();
    props.setBaseUrl(server.url("/").toString());
    props.setApiKey("test-api-key");
    return props;
  }

  @Test
  void validate_postsToTheValidateEndpointWithKeyAndSdkHeader() throws Exception {
    server.enqueue(jsonResponse(VALID_BODY));

    ValidationResult result = clientFor(propsPointingAtServer()).validate("email",
        "USER@Example.Com");

    RecordedRequest request = server.takeRequest(2, TimeUnit.SECONDS);
    assertThat(request).isNotNull();
    assertThat(request.getMethod()).isEqualTo("POST");
    assertThat(request.getPath()).isEqualTo("/api/v1/validate/email");
    assertThat(request.getHeader("X-API-KEY")).isEqualTo("test-api-key");
    assertThat(request.getHeader("X-VerifNow-SDK")).startsWith("java/");
    assertThat(request.getBody().readUtf8()).isEqualTo("{\"value\":\"USER@Example.Com\"}");

    assertThat(result.isValid()).isTrue();
    assertThat(result.getNormalizedValue()).isEqualTo("user@example.com");
    assertThat(result.getValidationLevel()).isEqualTo("STANDARD");
  }

  @Test
  void validateAsync_usesTheSameEndpointAsValidate() throws Exception {
    // Regression guard for 2.1.1: validateAsync used to call "/api/v1/{rule}", missing the
    // "validate" segment, so every asynchronous call 404'd.
    server.enqueue(jsonResponse(VALID_BODY));

    ValidationResult result = clientFor(propsPointingAtServer())
        .validateAsync("email", "user@example.com")
        .get(5, TimeUnit.SECONDS);

    RecordedRequest request = server.takeRequest(2, TimeUnit.SECONDS);
    assertThat(request).isNotNull();
    assertThat(request.getPath()).isEqualTo("/api/v1/validate/email");
    assertThat(result.isValid()).isTrue();
  }

  @Test
  void validate_routesEachRuleToItsOwnEndpoint() throws Exception {
    for (String rule : new String[] {"email", "phone", "iban", "vat", "nas", "ssn", "nif"}) {
      server.enqueue(jsonResponse(VALID_BODY));
      clientFor(propsPointingAtServer()).validate(rule, "value");

      RecordedRequest request = server.takeRequest(2, TimeUnit.SECONDS);
      assertThat(request).isNotNull();
      assertThat(request.getPath()).isEqualTo("/api/v1/validate/" + rule);
    }
  }

  @Test
  void validate_failsOpenWhenTheApiIsUnreachable() throws IOException {
    ValidationProperties props = propsPointingAtServer();
    props.setTimeoutMs(200);
    server.shutdown(); // nothing is listening any more

    ValidationResult result = clientFor(props).validate("email", "user@example.com");

    // Documented behaviour: accept unverified rather than block the caller's flow.
    assertThat(result.isValid()).isTrue();
    assertThat(result.getMessage()).isEqualTo("fallback-permit");
  }

  @Test
  void validate_failsClosedWhenFailOnErrorIsSet() throws IOException {
    ValidationProperties props = propsPointingAtServer();
    props.setTimeoutMs(200);
    props.setFailOnError(true);
    server.shutdown();

    assertThatThrownBy(() -> clientFor(props).validate("email", "user@example.com"))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("VerifNow API error");
  }

  private static final String FRANCE_BODY = """
      {"countryCode":"FR","standardRate":20,"reducedRates":[2.1,5.5,10],
       "regionalRates":[{"rate":8.5,"note":"The standard VAT rate in Martinique, Guadeloupe and Réunion is 8.5%."},
                        {"rate":13,"note":"For Corsica: rate of 13% on oil products."}],
       "situationOn":"2026-07-01","fetchedAt":"2026-09-23T02:52:37Z","addedLater":true}""";

  @Test
  void vatRate_getsOneMemberStateAndMapsItsRates() throws Exception {
    server.enqueue(jsonResponse(FRANCE_BODY));

    CountryVatRates france = clientFor(propsPointingAtServer()).vatRate(" FR ");

    RecordedRequest request = server.takeRequest(2, TimeUnit.SECONDS);
    assertThat(request).isNotNull();
    assertThat(request.getMethod()).isEqualTo("GET");
    assertThat(request.getPath()).isEqualTo("/api/v1/vat/rates/FR");
    assertThat(request.getHeader("X-VerifNow-SDK")).startsWith("java/");

    assertThat(france.countryCode()).isEqualTo("FR");
    assertThat(france.standardRate()).isEqualByComparingTo("20");
    assertThat(france.reducedRates()).extracting(BigDecimal::toPlainString)
        .containsExactly("2.1", "5.5", "10");
    assertThat(france.regionalRates()).extracting(RegionalVatRate::rate)
        .extracting(BigDecimal::toPlainString).containsExactly("8.5", "13");
    assertThat(france.regionalRates().get(1).note()).startsWith("For Corsica");
    assertThat(france.situationOn()).isEqualTo(LocalDate.of(2026, 7, 1));
    assertThat(france.fetchedAt()).isEqualTo(Instant.parse("2026-09-23T02:52:37Z"));
  }

  @Test
  void vatRates_getsEveryMemberState() throws Exception {
    server.enqueue(jsonResponse("""
        {"source":"TEDB","sourceUrl":"https://ec.europa.eu/taxation_customs/tedb/","countries":2,
         "rates":[%s,{"countryCode":"DK","standardRate":25,"reducedRates":[],"regionalRates":[],
                      "situationOn":"2026-07-01"}]}""".formatted(FRANCE_BODY)));

    VatRates all = clientFor(propsPointingAtServer()).vatRates();

    RecordedRequest request = server.takeRequest(2, TimeUnit.SECONDS);
    assertThat(request).isNotNull();
    assertThat(request.getPath()).isEqualTo("/api/v1/vat/rates");
    assertThat(all.source()).isEqualTo("TEDB");
    assertThat(all.rates()).extracting(CountryVatRates::countryCode).containsExactly("FR", "DK");
    assertThat(all.rates().get(1).reducedRates()).isEmpty();
  }

  @Test
  void vatRate_throwsNotFoundForACountryOutsideTheUnion() {
    server.enqueue(new MockResponse().setResponseCode(404)
        .setHeader("Content-Type", "application/json")
        .setBody("{\"status\":404,\"message\":\"Not an EU member state: US.\"}"));

    assertThatThrownBy(() -> clientFor(propsPointingAtServer()).vatRate("US"))
        .isInstanceOf(WebClientResponseException.NotFound.class);
  }

  @Test
  void vatRate_neverFailsOpen() throws IOException {
    // failOnError=false lets a validation through unverified. A rate has no such safe default:
    // returning one would mean inventing a tax rate, so the call throws whatever the setting.
    ValidationProperties props = propsPointingAtServer();
    props.setTimeoutMs(200);
    props.setFailOnError(false);
    server.shutdown();

    assertThatThrownBy(() -> clientFor(props).vatRate("FR")).isInstanceOf(RuntimeException.class);
    assertThatThrownBy(() -> clientFor(props).vatRates()).isInstanceOf(RuntimeException.class);
  }

  @Test
  void vatRate_rejectsABlankCodeWithoutCallingTheApi() {
    assertThatThrownBy(() -> clientFor(propsPointingAtServer()).vatRate("  "))
        .isInstanceOf(IllegalArgumentException.class);
    assertThat(server.getRequestCount()).isZero();
  }

  @Test
  void validateVat_sendsTheTraderNameAndReadsTheMatch() throws Exception {
    server.enqueue(jsonResponse("""
        {"valid":true,"message":"Valid VAT number","normalizedValue":"ESA28015865",
         "vatDetails":{"format_valid":true,"registered":true,"country_code":"ES","source":"LIVE",
           "vies_available":true,"trader_name_match":"MATCH","trader_name_match_source":"VIES"}}"""));

    ValidationResult result = clientFor(propsPointingAtServer())
        .validateVat("ESA28015865", "Telefonica");

    RecordedRequest request = server.takeRequest(2, TimeUnit.SECONDS);
    assertThat(request).isNotNull();
    assertThat(request.getPath()).isEqualTo("/api/v1/validate/vat");
    assertThat(request.getBody().readUtf8())
        .isEqualTo("{\"value\":\"ESA28015865\",\"traderName\":\"Telefonica\"}");
    assertThat(result.getVatDetails().traderNameMatch()).isEqualTo(TraderNameMatch.MATCH);
    assertThat(result.getVatDetails().traderNameMatchSource())
        .isEqualTo(TraderNameMatchSource.VIES);
  }

  @Test
  void validateVat_omitsABlankTraderName() throws Exception {
    server.enqueue(jsonResponse(VALID_BODY));

    clientFor(propsPointingAtServer()).validateVat("IE6388047V", "  ");

    RecordedRequest request = server.takeRequest(2, TimeUnit.SECONDS);
    assertThat(request).isNotNull();
    assertThat(request.getBody().readUtf8()).isEqualTo("{\"value\":\"IE6388047V\"}");
  }

  @Test
  void validateVat_anUnknownMatchValueIsNullNotAnError() {
    server.enqueue(jsonResponse("""
        {"valid":true,"vatDetails":{"format_valid":true,"registered":true,
          "trader_name_match":"SOMETHING_NEW","vies_available":true}}"""));

    ValidationResult result = clientFor(propsPointingAtServer()).validateVat("IE6388047V", "X");

    assertThat(result.getVatDetails().traderNameMatch()).isNull();
  }

  @Test
  void vatRate_readsEuVatAreaOnRegionalRates() {
    server.enqueue(jsonResponse("""
        {"countryCode":"FR","standardRate":20,"reducedRates":[2.1,5.5,10],
         "regionalRates":[{"rate":0.9,"note":"For Corsica","euVatArea":true},
                          {"rate":8.5,"note":"Martinique, Guadeloupe and Réunion","euVatArea":false}],
         "situationOn":"2026-07-01"}"""));

    CountryVatRates france = clientFor(propsPointingAtServer()).vatRate("FR");

    assertThat(france.regionalRates()).extracting(RegionalVatRate::euVatArea)
        .containsExactly(true, false);
  }

  @Test
  void anExistingImplementationOfTheInterfaceStillCompilesAndSaysWhatItLacks() {
    // Written against 2.7.0: only the two validation methods. The rate methods are defaults, so
    // this still compiles in 2.8.0 — and says plainly that it does not read rates.
    VerifNowClient legacy = new VerifNowClient() {
      @Override
      public ValidationResult validate(String rule, String value) {
        return new ValidationResult();
      }

      @Override
      public java.util.concurrent.CompletableFuture<ValidationResult> validateAsync(String rule,
          String value) {
        return java.util.concurrent.CompletableFuture.completedFuture(new ValidationResult());
      }
    };

    assertThatThrownBy(legacy::vatRates).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> legacy.validateVat("IE6388047V", "Google Ireland Ltd"))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> legacy.vatRate("FR"))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  private static MockResponse jsonResponse(String body) {
    return new MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(body);
  }
}
