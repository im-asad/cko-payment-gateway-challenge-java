package com.checkout.payment.gateway.model;

public class BankSimulatorPostPaymentResponse {
  private boolean authorized;
  private String authorizationCode;

  public boolean getAuthorized() {
    return authorized;
  }

  public void setAuthorized(boolean authorized) {
    this.authorized = authorized;
  }

  public String getAuthorizationCode() {
    return authorizationCode;
  }

  public void setAuthorizationCode(String authorizationCode) {
    this.authorizationCode = authorizationCode;
  }

  @Override
  public String toString() {
    return "BankSimulatorPostPaymentResponse{" +
      "authorized='" + authorized + '\'' +
      "authorizationCode='" + authorizationCode + '\'' +
    "}";
  }
}
