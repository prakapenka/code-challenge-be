package localhost.challenge.adapter.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import localhost.challenge.adapter.rest.validation.ValidTransactionBalance;
import localhost.challenge.adapter.rest.validation.util.AccountValidationConst;

public record CreateTransactionDTO(
    // for simplicity same pattern as for account
    @NotNull @Pattern(regexp = AccountValidationConst.ACCOUNT_ID_PATTERN) String txId,
    @ValidTransactionBalance AccountBalanceDTO amount,
    @NotNull @Pattern(regexp = AccountValidationConst.ACCOUNT_ID_PATTERN) String from,
    @NotNull @Pattern(regexp = AccountValidationConst.ACCOUNT_ID_PATTERN) String to) {}
