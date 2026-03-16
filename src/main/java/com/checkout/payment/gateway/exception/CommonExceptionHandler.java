package com.checkout.payment.gateway.exception;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.api.CreatePaymentResponse;
import com.checkout.payment.gateway.model.api.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CommonExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CommonExceptionHandler.class);

  @ExceptionHandler(PaymentNotFoundException.class)
  public ResponseEntity<ErrorResponse> handlePaymentNotFoundException(PaymentNotFoundException ex) {
    LOG.warn("Payment not found: {}", ex.getMessage());
    return new ResponseEntity<>(new ErrorResponse("Payment not found"), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(BankCommunicationException.class)
  public ResponseEntity<ErrorResponse> handleBankCommunicationException(BankCommunicationException ex) {
    LOG.error("Bank communication failure: {}", ex.getMessage(), ex);
    return new ResponseEntity<>(new ErrorResponse("Payment processing unavailable, please try again later"), HttpStatus.BAD_GATEWAY);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<CreatePaymentResponse> handleValidationException(MethodArgumentNotValidException ex) {
    LOG.warn("Invalid payment request: {}", ex.getMessage());
    CreatePaymentResponse response = new CreatePaymentResponse();
    response.setStatus(PaymentStatus.REJECTED);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }
}
