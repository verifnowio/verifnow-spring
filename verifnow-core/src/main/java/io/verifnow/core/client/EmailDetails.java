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
 * Detailed email validation data returned by the VerifNow API.
 * Present only for email validation responses.
 */
public class EmailDetails {

  private EmailSignals signals;

  @JsonProperty("risk_score")
  private int riskScore;

  @JsonProperty("risk_level")
  private RiskLevel riskLevel;

  private Deliverability deliverability;

  @JsonProperty("applied_level")
  private String appliedLevel;

  public EmailDetails() {}

  public EmailSignals getSignals() {
    return signals;
  }

  public void setSignals(EmailSignals signals) {
    this.signals = signals;
  }

  public int getRiskScore() {
    return riskScore;
  }

  public void setRiskScore(int riskScore) {
    this.riskScore = riskScore;
  }

  public RiskLevel getRiskLevel() {
    return riskLevel;
  }

  public void setRiskLevel(RiskLevel riskLevel) {
    this.riskLevel = riskLevel;
  }

  public Deliverability getDeliverability() {
    return deliverability;
  }

  public void setDeliverability(Deliverability deliverability) {
    this.deliverability = deliverability;
  }

  public String getAppliedLevel() {
    return appliedLevel;
  }

  public void setAppliedLevel(String appliedLevel) {
    this.appliedLevel = appliedLevel;
  }
}

