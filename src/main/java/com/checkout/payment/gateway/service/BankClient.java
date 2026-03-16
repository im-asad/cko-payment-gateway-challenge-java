package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.model.bank.BankTransactionRequest;
import com.checkout.payment.gateway.model.bank.BankTransactionResponse;

public interface BankClient {

  BankTransactionResponse processPayment(BankTransactionRequest request);
}
