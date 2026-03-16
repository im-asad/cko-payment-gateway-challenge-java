package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.model.BankPaymentRequest;
import com.checkout.payment.gateway.model.BankResponse;
import com.checkout.payment.gateway.model.GetPaymentResponse;
import com.checkout.payment.gateway.model.Payment;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
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

  public GetPaymentResponse getPaymentById(UUID id) {
    LOG.debug("Requesting payment with ID {}", id);
    return paymentsRepository.get(id)
        .map(this::toGetPaymentResponse)
        .orElseThrow(() -> new PaymentNotFoundException(id));
  }

  public PostPaymentResponse processPayment(PostPaymentRequest paymentRequest) {
    BankResponse bankResponse = bankClient.processPayment(toBankPaymentRequest(paymentRequest));

    PaymentStatus status = bankResponse.isAuthorized() ? PaymentStatus.AUTHORIZED : PaymentStatus.DECLINED;
    UUID paymentId = UUID.randomUUID();

    Payment payment = new Payment();
    payment.setId(paymentId);
    payment.setStatus(status);
    payment.setCardNumber(paymentRequest.getCardNumber());
    payment.setExpiryMonth(paymentRequest.getExpiryMonth());
    payment.setExpiryYear(paymentRequest.getExpiryYear());
    payment.setCurrency(paymentRequest.getCurrency());
    payment.setAmount(paymentRequest.getAmount());
    paymentsRepository.add(payment);

    PostPaymentResponse response = new PostPaymentResponse();
    response.setPaymentId(paymentId);
    response.setPaymentStatus(status);
    return response;
  }

  private BankPaymentRequest toBankPaymentRequest(PostPaymentRequest request) {
    return new BankPaymentRequest(
        request.getCardNumber(),
        request.getExpiryDate(),
        request.getCurrency(),
        request.getAmount(),
        request.getCvv()
    );
  }

  private GetPaymentResponse toGetPaymentResponse(Payment payment) {
    GetPaymentResponse response = new GetPaymentResponse();
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
