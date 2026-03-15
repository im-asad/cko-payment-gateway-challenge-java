package com.checkout.payment.gateway.repository;

import com.checkout.payment.gateway.model.GetPaymentResponse;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentsRepository {

  private final HashMap<UUID, GetPaymentResponse> payments = new HashMap<>();

  public void add(GetPaymentResponse payment) {
    payments.put(payment.getId(), payment);
  }

  public Optional<GetPaymentResponse> get(UUID id) {
    return Optional.ofNullable(payments.get(id));
  }

}
