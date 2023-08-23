package localhost.challenge.adapter.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import localhost.challenge.adapter.rest.validation.UniqueNewAccount;
import localhost.challenge.adapter.rest.validation.ValidCreateBalance;
import localhost.challenge.adapter.rest.validation.util.AccountValidationConst;

@UniqueNewAccount
public record CreateAccountDTO(
    @NotNull @Pattern(regexp = AccountValidationConst.ACCOUNT_ID_PATTERN) String accountId,
    @Valid @ValidCreateBalance AccountBalanceDTO balance) {}
