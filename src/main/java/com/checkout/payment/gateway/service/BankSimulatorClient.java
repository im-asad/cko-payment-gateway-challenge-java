package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.model.BankSimulatorRequest;
import com.checkout.payment.gateway.model.BankSimulatorResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BankSimulatorClient {

  private static final Logger LOG = LoggerFactory.getLogger(BankSimulatorClient.class);

  private final RestTemplate restTemplate;
  private final String bankSimulatorUrl;

  public BankSimulatorClient(@Value("${bank.simulator.url}") String bankSimulatorUrl) {
    this.restTemplate = new RestTemplate();
    this.bankSimulatorUrl = bankSimulatorUrl;
  }

  public BankSimulatorResponse processPayment(PostPaymentRequest request) {
    BankSimulatorRequest bankRequest = new BankSimulatorRequest(
        request.getCardNumber(),
        request.getExpiryDate(),
        request.getCurrency(),
        request.getAmount(),
        request.getCvv()
    );

    LOG.debug("Sending payment request to bank simulator");

    return restTemplate.postForObject(bankSimulatorUrl + "/payments", bankRequest, BankSimulatorResponse.class);
  }
}
