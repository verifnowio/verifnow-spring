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
package io.verifnow.core.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.Optional;

/**
 * Detailed signal data returned by the VerifNow email validation API.
 */
public class EmailSignals {

  @JsonProperty("syntax_valid")
  private boolean syntaxValid;

  @JsonProperty("mx_valid")
  private boolean mxValid;

  @JsonProperty("typo_detected")
  private boolean typoDetected;

  @JsonProperty("suggested_domain")
  private String suggestedDomain;

  private boolean disposable;

  @JsonProperty("role_based")
  private boolean roleBased;

  // Boxed: the API omits this field below ADVANCED depth, and a primitive would read that as false.
  @JsonProperty("free_provider")
  private Boolean freeProvider;

  @JsonProperty("domain_age_days")
  private Integer domainAgeDays;

  @JsonProperty("mx_provider")
  private String mxProvider;

  @JsonProperty("mx_quality_score")
  private double mxQualityScore;

  public EmailSignals() {}

  public boolean isSyntaxValid() {
    return syntaxValid;
  }

  public void setSyntaxValid(boolean syntaxValid) {
    this.syntaxValid = syntaxValid;
  }

  public boolean isMxValid() {
    return mxValid;
  }

  public void setMxValid(boolean mxValid) {
    this.mxValid = mxValid;
  }

  public boolean isTypoDetected() {
    return typoDetected;
  }

  public void setTypoDetected(boolean typoDetected) {
    this.typoDetected = typoDetected;
  }

  public boolean isDisposable() {
    return disposable;
  }

  public void setDisposable(boolean disposable) {
    this.disposable = disposable;
  }

  public boolean isRoleBased() {
    return roleBased;
  }

  public void setRoleBased(boolean roleBased) {
    this.roleBased = roleBased;
  }

  /**
   * Whether the domain is a consumer mailbox provider.
   *
   * <p>Returns {@code false} when the signal was not computed, which happens on the FREE and
   * STARTER plans. Use {@link #freeProviderIfComputed()} to tell the two apart.
   */
  public boolean isFreeProvider() {
    return Boolean.TRUE.equals(freeProvider);
  }

  public void setFreeProvider(boolean freeProvider) {
    this.freeProvider = freeProvider;
  }

  // Jackson's entry point for free_provider. The public setter takes a primitive, so an explicit
  // JSON null would reach it as false and erase the difference between "no" and "not computed".
  @JsonSetter("free_provider")
  private void readFreeProvider(Boolean freeProvider) {
    this.freeProvider = freeProvider;
  }

  /**
   * The free-provider signal, or empty when the applied validation level did not compute it
   * (below ADVANCED, i.e. FREE and STARTER). Empty is not "not a free provider".
   *
   * @since 2.2.0
   */
  public Optional<Boolean> freeProviderIfComputed() {
    return Optional.ofNullable(freeProvider);
  }

  /**
   * The corrected domain proposed when {@link #isTypoDetected()} is true, e.g. {@code gmail.com}.
   *
   * @since 2.2.0
   */
  public String getSuggestedDomain() {
    return suggestedDomain;
  }

  public void setSuggestedDomain(String suggestedDomain) {
    this.suggestedDomain = suggestedDomain;
  }

  /**
   * Estimated age of the domain in days, or {@code null} below ADVANCED depth.
   *
   * @since 2.2.0
   */
  public Integer getDomainAgeDays() {
    return domainAgeDays;
  }

  public void setDomainAgeDays(Integer domainAgeDays) {
    this.domainAgeDays = domainAgeDays;
  }

  public String getMxProvider() {
    return mxProvider;
  }

  public void setMxProvider(String mxProvider) {
    this.mxProvider = mxProvider;
  }

  public double getMxQualityScore() {
    return mxQualityScore;
  }

  public void setMxQualityScore(double mxQualityScore) {
    this.mxQualityScore = mxQualityScore;
  }
}

