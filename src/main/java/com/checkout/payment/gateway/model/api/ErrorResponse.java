package com.checkout.payment.gateway.model.api;

public class ErrorResponse {

  private final String message;

  public ErrorResponse(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
