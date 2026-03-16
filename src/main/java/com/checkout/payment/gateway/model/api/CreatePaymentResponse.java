package com.checkout.payment.gateway.model.api;

import com.checkout.payment.gateway.enums.PaymentStatus;
import java.util.UUID;

public class CreatePaymentResponse {

  private UUID id;
  private PaymentStatus status;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public void setStatus(PaymentStatus status) {
    this.status = status;
  }
}
