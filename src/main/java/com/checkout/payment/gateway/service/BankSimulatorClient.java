package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.exception.BankCommunicationException;
import com.checkout.payment.gateway.model.bank.BankTransactionRequest;
import com.checkout.payment.gateway.model.bank.BankTransactionResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
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
  public BankTransactionResponse processPayment(BankTransactionRequest request) {
    SimulatorRequest simulatorRequest = toSimulatorRequest(request);

    LOG.debug("Sending payment request to bank simulator");

    try {
      SimulatorResponse simulatorResponse =
          restTemplate.postForObject(bankSimulatorUrl + "/payments", simulatorRequest, SimulatorResponse.class);

      if (simulatorResponse == null) {
        throw new BankCommunicationException("Bank simulator returned an empty response", null);
      }

      return toResponse(simulatorResponse);
    } catch (RestClientException ex) {
      LOG.error("Failed to communicate with bank simulator", ex);
      throw new BankCommunicationException("Failed to communicate with bank simulator", ex);
    }
  }

  // Adapter: translates our generic BankTransactionRequest into the simulator's specific format
  private SimulatorRequest toSimulatorRequest(BankTransactionRequest request) {
    return new SimulatorRequest(
        request.getCardNumber(),
        request.getExpiryDate(),
        request.getCurrency(),
        request.getAmount(),
        request.getCvv()
    );
  }

  // Adapter: translates the simulator's specific response back into our generic BankTransactionResponse
  private BankTransactionResponse toResponse(SimulatorResponse simulatorResponse) {
    BankTransactionResponse response = new BankTransactionResponse();
    response.setAuthorized(simulatorResponse.authorized);
    response.setAuthorizationCode(simulatorResponse.authorizationCode);
    return response;
  }

  // Simulator-specific HTTP request DTO — implementation detail, not part of the public model
  private static class SimulatorRequest {

    @JsonProperty("card_number")
    private final String cardNumber;

    @JsonProperty("expiry_date")
    private final String expiryDate;

    private final String currency;
    private final int amount;
    private final String cvv;

    private SimulatorRequest(String cardNumber, String expiryDate, String currency, int amount, String cvv) {
      this.cardNumber = cardNumber;
      this.expiryDate = expiryDate;
      this.currency = currency;
      this.amount = amount;
      this.cvv = cvv;
    }

    public String getCardNumber() { return cardNumber; }
    public String getExpiryDate() { return expiryDate; }
    public String getCurrency() { return currency; }
    public int getAmount() { return amount; }
    public String getCvv() { return cvv; }
  }

  // Simulator-specific HTTP response DTO — implementation detail, not part of the public model
  private static class SimulatorResponse {

    private boolean authorized;

    @JsonProperty("authorization_code")
    private String authorizationCode;

    public boolean isAuthorized() { return authorized; }
    public void setAuthorized(boolean authorized) { this.authorized = authorized; }
    public String getAuthorizationCode() { return authorizationCode; }
    public void setAuthorizationCode(String authorizationCode) { this.authorizationCode = authorizationCode; }
  }
}
