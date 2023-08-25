package localhost.challenge.adapter.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import javax.money.MonetaryAmount;
import localhost.challenge.adapter.rest.validation.UniqueNewAccount;
import localhost.challenge.adapter.rest.validation.ValidCreateBalance;
import localhost.challenge.adapter.rest.validation.util.AccountValidationConst;

@UniqueNewAccount
public record CreateAccountDTO(
    @NotNull @Pattern(regexp = AccountValidationConst.ACCOUNT_ID_PATTERN) String accountId,
    @ValidCreateBalance MonetaryAmount balance) {}
