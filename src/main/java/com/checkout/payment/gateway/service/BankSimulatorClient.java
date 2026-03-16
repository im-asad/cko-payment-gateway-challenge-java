package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.exception.BankCommunicationException;
import com.checkout.payment.gateway.model.BankPaymentRequest;
import com.checkout.payment.gateway.model.BankResponse;
import com.checkout.payment.gateway.model.BankSimulatorRequest;
import com.checkout.payment.gateway.model.BankSimulatorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class BankSimulatorClient implements BankClient {

  private static final Logger LOG = LoggerFactory.getLogger(BankSimulatorClient.class);

  private final RestTemplate restTemplate;
  private final String bankSimulatorUrl;

  public BankSimulatorClient(RestTemplate restTemplate, @Value("${bank.simulator.url}") String bankSimulatorUrl) {
    this.restTemplate = restTemplate;
    this.bankSimulatorUrl = bankSimulatorUrl;
  }

  @Override
  public BankResponse processPayment(BankPaymentRequest request) {
    BankSimulatorRequest simulatorRequest = toSimulatorRequest(request);

    LOG.debug("Sending payment request to bank simulator");

    try {
      BankSimulatorResponse simulatorResponse =
          restTemplate.postForObject(bankSimulatorUrl + "/payments", simulatorRequest, BankSimulatorResponse.class);

      if (simulatorResponse == null) {
        throw new BankCommunicationException("Bank simulator returned an empty response", null);
      }

      return toResponse(simulatorResponse);
    } catch (RestClientException ex) {
      LOG.error("Failed to communicate with bank simulator", ex);
      throw new BankCommunicationException("Failed to communicate with bank simulator", ex);
    }
  }

  // Adapter: translates our generic BankPaymentRequest into the simulator's specific format
  private BankSimulatorRequest toSimulatorRequest(BankPaymentRequest request) {
    return new BankSimulatorRequest(
        request.getCardNumber(),
        request.getExpiryDate(),
        request.getCurrency(),
        request.getAmount(),
        request.getCvv()
    );
  }

  // Adapter: translates the simulator's specific response back into our generic BankResponse
  private BankResponse toResponse(BankSimulatorResponse simulatorResponse) {
    BankResponse response = new BankResponse();
    response.setAuthorized(simulatorResponse.getIsAuthorized());
    response.setAuthorizationCode(simulatorResponse.getAuthorizationCode());
    return response;
  }
}
