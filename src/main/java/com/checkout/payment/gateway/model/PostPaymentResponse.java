package com.checkout.payment.gateway.model;

import java.util.UUID;

import com.checkout.payment.gateway.enums.PaymentStatus;

public class PostPaymentResponse {

  private UUID id;
  private PaymentStatus paymentStatus;

  public UUID getPaymentId() {
    return id;
  }

  public void setPaymentId(UUID id) {
    this.id = id;
  }

  public PaymentStatus getPaymentStatus() {
    return paymentStatus;
  }

  public void setPaymentStatus(PaymentStatus paymentStatus) {
    this.paymentStatus = paymentStatus;
  }
}
