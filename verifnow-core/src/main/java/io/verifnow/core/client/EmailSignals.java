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

  private boolean disposable;

  @JsonProperty("role_based")
  private boolean roleBased;

  @JsonProperty("free_provider")
  private boolean freeProvider;

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

  public boolean isFreeProvider() {
    return freeProvider;
  }

  public void setFreeProvider(boolean freeProvider) {
    this.freeProvider = freeProvider;
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

