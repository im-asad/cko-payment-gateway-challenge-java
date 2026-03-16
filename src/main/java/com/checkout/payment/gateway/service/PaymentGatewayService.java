package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.model.api.CreatePaymentRequest;
import com.checkout.payment.gateway.model.api.CreatePaymentResponse;
import com.checkout.payment.gateway.model.api.PaymentResponse;
import com.checkout.payment.gateway.model.bank.BankTransactionRequest;
import com.checkout.payment.gateway.model.bank.BankTransactionResponse;
import com.checkout.payment.gateway.model.domain.Payment;
import com.checkout.payment.gateway.repository.PaymentsRepository;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  private final PaymentsRepository paymentsRepository;
  private final BankClient bankClient;

  public PaymentGatewayService(PaymentsRepository paymentsRepository, BankClient bankClient) {
    this.paymentsRepository = paymentsRepository;
    this.bankClient = bankClient;
  }

  public PaymentResponse getPaymentById(UUID id) {
    LOG.debug("Requesting payment with ID {}", id);
    return paymentsRepository.get(id)
        .map(this::toPaymentResponse)
        .orElseThrow(() -> new PaymentNotFoundException(id));
  }

  public CreatePaymentResponse processPayment(CreatePaymentRequest request) {
    BankTransactionResponse bankResponse = bankClient.processPayment(toBankTransactionRequest(request));

    PaymentStatus status = bankResponse.isAuthorized() ? PaymentStatus.AUTHORIZED : PaymentStatus.DECLINED;
    UUID paymentId = UUID.randomUUID();

    Payment payment = new Payment();
    payment.setId(paymentId);
    payment.setStatus(status);
    payment.setCardNumber(request.getCardNumber());
    payment.setExpiryMonth(request.getExpiryMonth());
    payment.setExpiryYear(request.getExpiryYear());
    payment.setCurrency(request.getCurrency());
    payment.setAmount(request.getAmount());
    paymentsRepository.add(payment);

    CreatePaymentResponse response = new CreatePaymentResponse();
    response.setId(paymentId);
    response.setStatus(status);
    return response;
  }

  private BankTransactionRequest toBankTransactionRequest(CreatePaymentRequest request) {
    return new BankTransactionRequest(
        request.getCardNumber(),
        request.getExpiryDate(),
        request.getCurrency(),
        request.getAmount(),
        request.getCvv()
    );
  }

  private PaymentResponse toPaymentResponse(Payment payment) {
    PaymentResponse response = new PaymentResponse();
    response.setId(payment.getId());
    response.setStatus(payment.getStatus());
    response.setCardNumberLastFour(Integer.parseInt(
        payment.getCardNumber().substring(payment.getCardNumber().length() - 4)));
    response.setExpiryMonth(payment.getExpiryMonth());
    response.setExpiryYear(payment.getExpiryYear());
    response.setCurrency(payment.getCurrency());
    response.setAmount(payment.getAmount());
    return response;
  }
}
