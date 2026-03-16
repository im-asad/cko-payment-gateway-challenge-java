package com.checkout.payment.gateway.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.BankCommunicationException;
import com.checkout.payment.gateway.model.bank.BankTransactionRequest;
import com.checkout.payment.gateway.model.bank.BankTransactionResponse;
import com.checkout.payment.gateway.model.domain.Payment;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import com.checkout.payment.gateway.service.BankClient;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentGatewayControllerTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private PaymentsRepository paymentsRepository;

  @MockBean
  private BankClient bankClient;

  @Test
  void getPayment_whenPaymentExists_returns200WithCorrectBody() throws Exception {
    Payment payment = storedPayment("4111111111111234", PaymentStatus.AUTHORIZED);
    paymentsRepository.add(payment);

    mvc.perform(get("/payment/" + payment.getId()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(payment.getId().toString()))
      .andExpect(jsonPath("$.status").value("Authorized"))
      .andExpect(jsonPath("$.cardNumberLastFour").value(1234))
      .andExpect(jsonPath("$.expiryMonth").value(12))
      .andExpect(jsonPath("$.expiryYear").value(2030))
      .andExpect(jsonPath("$.currency").value("GBP"))
      .andExpect(jsonPath("$.amount").value(100));
  }

  @Test
  void getPayment_whenPaymentDoesNotExist_returns404() throws Exception {
    mvc.perform(get("/payment/" + UUID.randomUUID()))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.message").value("Payment not found"));
  }

  @Test
  void processPayment_whenAuthorized_returns201WithAuthorizedStatus() throws Exception {
    when(bankClient.processPayment(any(BankTransactionRequest.class))).thenReturn(authorizedBankResponse());

    mvc.perform(post("/payment")
      .contentType(MediaType.APPLICATION_JSON)
      .content(validPaymentRequestJson("4111111111111111")))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id").isNotEmpty())
      .andExpect(jsonPath("$.status").value("Authorized"));
  }

  @Test
  void processPayment_whenDeclined_returns201WithDeclinedStatus() throws Exception {
    when(bankClient.processPayment(any(BankTransactionRequest.class))).thenReturn(declinedBankResponse());

    mvc.perform(post("/payment")
      .contentType(MediaType.APPLICATION_JSON)
      .content(validPaymentRequestJson("4111111111111111")))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.status").value("Declined"));
  }

  @Test
  void processPayment_whenRequestIsInvalid_returns400WithRejectedStatus() throws Exception {
    String missingCardNumber = """
      {
        "expiry_month": 12,
        "expiry_year": 2030,
        "currency": "GBP",
        "amount": 100,
        "cvv": "123"
      }
      """;

    mvc.perform(post("/payment")
      .contentType(MediaType.APPLICATION_JSON)
      .content(missingCardNumber))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value("Rejected"));
  }

  @Test
  void processPayment_whenCardNumberIsNotNumeric_returns400WithRejectedStatus() throws Exception {
    mvc.perform(post("/payment")
      .contentType(MediaType.APPLICATION_JSON)
      .content(validPaymentRequestJson("411111111111ABCD")))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value("Rejected"));
  }

  @Test
  void processPayment_whenCardIsExpired_returns400WithRejectedStatus() throws Exception {
    String expiredCard = """
      {
        "card_number": "4111111111111111",
        "expiry_month": 2,
        "expiry_year": 2026,
        "currency": "GBP",
        "amount": 100,
        "cvv": "123"
      }
      """;

    mvc.perform(post("/payment")
      .contentType(MediaType.APPLICATION_JSON)
      .content(expiredCard))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value("Rejected"));
  }

  @Test
  void processPayment_whenBankIsUnavailable_returns502() throws Exception {
    when(bankClient.processPayment(any(BankTransactionRequest.class)))
      .thenThrow(new BankCommunicationException("Bank unavailable", null));

    mvc.perform(post("/payment")
      .contentType(MediaType.APPLICATION_JSON)
      .content(validPaymentRequestJson("4111111111111111")))
      .andExpect(status().isBadGateway())
      .andExpect(jsonPath("$.message").value("Payment processing unavailable, please try again later"));
  }

  private Payment storedPayment(String cardNumber, PaymentStatus status) {
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

  private BankTransactionResponse authorizedBankResponse() {
    BankTransactionResponse response = new BankTransactionResponse();
    response.setAuthorized(true);
    response.setAuthorizationCode("AUTH-001");
    return response;
  }

  private BankTransactionResponse declinedBankResponse() {
    BankTransactionResponse response = new BankTransactionResponse();
    response.setAuthorized(false);
    return response;
  }

  private String validPaymentRequestJson(String cardNumber) {
    return """
      {
        "card_number": "%s",
        "expiry_month": 12,
        "expiry_year": 2030,
        "currency": "GBP",
        "amount": 100,
        "cvv": "123"
      }
      """.formatted(cardNumber);
  }
}
