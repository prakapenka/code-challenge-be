package localhost.challenge.adapter.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import localhost.challenge.adapter.rest.dto.AccountBalanceDTO;
import localhost.challenge.service.AllowedCurrencyService;
import localhost.challenge.service.AllowedTransactionService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TransactionBalanceValidator
    implements ConstraintValidator<ValidTransactionBalance, AccountBalanceDTO> {

  private final AllowedCurrencyService allowedCurrencyService;

  private final AllowedTransactionService allowedTransactionService;

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
