package localhost.challenge.adapter.rest.mapper;

import java.math.BigDecimal;
import javax.money.MonetaryAmount;
import localhost.challenge.adapter.rest.dto.AccountBalanceDTO;
import localhost.challenge.adapter.rest.dto.AccountDTO;
import localhost.challenge.adapter.rest.dto.CreateAccountDTO;
import localhost.challenge.domain.Account;
import org.javamoney.moneta.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface AccountDTOMapper {

  @Mapping(source = "balance", target = "amount")
  Account mapToDomain(CreateAccountDTO dto);

  @Mapping(source = "amount", target = "balance")
  AccountDTO mapToDTO(Account account);

  default MonetaryAmount fromBalanceDTO(AccountBalanceDTO dto) {
    return Money.of(dto.amount(), dto.currencyUnit());
  }

  default AccountBalanceDTO fromMonetaryAmount(MonetaryAmount amount) {
    return new AccountBalanceDTO(
        amount.getNumber().numberValueExact(BigDecimal.class), amount.getCurrency());
  }
}
