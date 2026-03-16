package com.checkout.payment.gateway.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.BankCommunicationException;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.model.api.CreatePaymentRequest;
import com.checkout.payment.gateway.model.api.CreatePaymentResponse;
import com.checkout.payment.gateway.model.api.GetPaymentResponse;
import com.checkout.payment.gateway.model.bank.BankTransactionRequest;
import com.checkout.payment.gateway.model.bank.BankTransactionResponse;
import com.checkout.payment.gateway.model.domain.Payment;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayServiceTest {

  @Mock
  private PaymentsRepository paymentsRepository;

  @Mock
  private BankClient bankClient;

  @InjectMocks
  private PaymentGatewayService service;

  @Test
  void processPayment_whenBankAuthorizes_storesPaymentWithAuthorizedStatus() {
    CreatePaymentRequest request = buildRequest("4111111111111111");
    when(bankClient.processPayment(any(BankTransactionRequest.class))).thenReturn(authorizedResponse());

    CreatePaymentResponse response = service.processPayment(request);

    assertThat(response.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
    assertThat(response.getId()).isNotNull();

    ArgumentCaptor<Payment> captor = forClass(Payment.class);
    verify(paymentsRepository).add(captor.capture());
    Payment stored = captor.getValue();
    assertThat(stored.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
    assertThat(stored.getCardNumber()).isEqualTo("4111111111111111");
    assertThat(stored.getCurrency()).isEqualTo("GBP");
    assertThat(stored.getAmount()).isEqualTo(100);
  }

  @Test
  void processPayment_whenBankDeclines_storesPaymentWithDeclinedStatus() {
    CreatePaymentRequest request = buildRequest("4111111111111111");
    when(bankClient.processPayment(any(BankTransactionRequest.class))).thenReturn(declinedResponse());

    CreatePaymentResponse response = service.processPayment(request);

    assertThat(response.getStatus()).isEqualTo(PaymentStatus.DECLINED);
    assertThat(response.getId()).isNotNull();

    ArgumentCaptor<Payment> captor = forClass(Payment.class);
    verify(paymentsRepository).add(captor.capture());
    assertThat(captor.getValue().getStatus()).isEqualTo(PaymentStatus.DECLINED);
  }

  @Test
  void processPayment_whenBankFails_throwsBankCommunicationException() {
    CreatePaymentRequest request = buildRequest("4111111111111111");
    when(bankClient.processPayment(any())).thenThrow(new BankCommunicationException("Bank unavailable", null));

    assertThatThrownBy(() -> service.processPayment(request))
      .isInstanceOf(BankCommunicationException.class)
      .hasMessageContaining("Bank unavailable");
  }

  @Test
  void getPaymentById_whenPaymentExists_returnsMaskedCardAndCorrectFields() {
    Payment payment = buildStoredPayment("4111111111111234", PaymentStatus.AUTHORIZED);
    when(paymentsRepository.get(payment.getId())).thenReturn(Optional.of(payment));

    GetPaymentResponse response = service.getPaymentById(payment.getId());

    assertThat(response.getId()).isEqualTo(payment.getId());
    assertThat(response.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
    assertThat(response.getCardNumberLastFour()).isEqualTo(1234);
    assertThat(response.getExpiryMonth()).isEqualTo(12);
    assertThat(response.getExpiryYear()).isEqualTo(2030);
    assertThat(response.getCurrency()).isEqualTo("GBP");
    assertThat(response.getAmount()).isEqualTo(100);
  }

  @Test
  void getPaymentById_whenPaymentDoesNotExist_throwsPaymentNotFoundException() {
    UUID unknownId = UUID.randomUUID();
    when(paymentsRepository.get(unknownId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getPaymentById(unknownId))
        .isInstanceOf(PaymentNotFoundException.class);
  }

  private CreatePaymentRequest buildRequest(String cardNumber) {
    CreatePaymentRequest request = new CreatePaymentRequest();
    request.setCardNumber(cardNumber);
    request.setExpiryMonth(12);
    request.setExpiryYear(2030);
    request.setCurrency("GBP");
    request.setAmount(100);
    request.setCvv("123");
    return request;
  }

  private Payment buildStoredPayment(String cardNumber, PaymentStatus status) {
    Payment payment = new Payment();
    payment.setId(UUID.randomUUID());
    payment.setCardNumber(cardNumber);
    payment.setStatus(status);
    payment.setExpiryMonth(12);
    payment.setExpiryYear(2030);
    payment.setCurrency("GBP");
    payment.setAmount(100);
    return payment;
  }

  private BankTransactionResponse authorizedResponse() {
    BankTransactionResponse response = new BankTransactionResponse();
    response.setAuthorized(true);
    response.setAuthorizationCode("AUTH-123");
    return response;
  }

  private BankTransactionResponse declinedResponse() {
    BankTransactionResponse response = new BankTransactionResponse();
    response.setAuthorized(false);
    return response;
  }
}
