package localhost.challenge.adapter.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import javax.money.MonetaryAmount;
import localhost.challenge.service.port.IsAllowedCurrency;
import localhost.challenge.service.port.IsAllowedTransaction;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateBalanceValidator
    implements ConstraintValidator<ValidCreateBalance, MonetaryAmount> {

  private final IsAllowedCurrency allowedCurrencyService;

  private final IsAllowedTransaction allowedTransactionService;

  @Override
  public boolean isValid(MonetaryAmount balance, ConstraintValidatorContext context) {
    final var actualCurrencyUnit = balance.getCurrency();
    if (allowedCurrencyService.isRejected(actualCurrencyUnit)) {
      return false;
    }
    return allowedTransactionService.isAllowedTransaction(balance);
  }
}
