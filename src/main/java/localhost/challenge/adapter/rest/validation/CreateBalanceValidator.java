package localhost.challenge.adapter.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import localhost.challenge.adapter.rest.dto.AccountBalanceDTO;
import localhost.challenge.service.port.IsAllowedCurrency;
import localhost.challenge.service.port.IsAllowedTransaction;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateBalanceValidator
    implements ConstraintValidator<ValidCreateBalance, AccountBalanceDTO> {

  private final IsAllowedCurrency allowedCurrencyService;

  private final IsAllowedTransaction allowedTransactionService;

  @Override
  public boolean isValid(AccountBalanceDTO balanceDTO, ConstraintValidatorContext context) {
    final var actualCurrencyUnit = balanceDTO.currencyUnit();

    if (allowedCurrencyService.isRejected(actualCurrencyUnit)) {
      return false;
    }

    return allowedTransactionService.isAllowedTransaction(
        balanceDTO.amount(), balanceDTO.currencyUnit());
  }
}