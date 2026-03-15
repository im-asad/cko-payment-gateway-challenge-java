package com.checkout.payment.gateway.validation;

import com.checkout.payment.gateway.model.PostPaymentRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.YearMonth;

public class ExpiryDateValidator implements ConstraintValidator<ValidExpiryDate, PostPaymentRequest> {

  @Override
  public boolean isValid(PostPaymentRequest request, ConstraintValidatorContext context) {
    if (request.getExpiryMonth() == null || request.getExpiryYear() == null) {
      return true; // let @NotNull handle missing values
    }
    if (request.getExpiryMonth() < 1 || request.getExpiryMonth() > 12) {
      return true; // let @Min/@Max handle invalid month value
    }
    YearMonth expiry = YearMonth.of(request.getExpiryYear(), request.getExpiryMonth());
    return expiry.isAfter(YearMonth.now());
  }
}
