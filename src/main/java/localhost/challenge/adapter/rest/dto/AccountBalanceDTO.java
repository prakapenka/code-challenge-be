package localhost.challenge.adapter.rest.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import javax.money.CurrencyUnit;

public record AccountBalanceDTO(@NotNull BigDecimal amount, @NotNull CurrencyUnit currencyUnit) {}
