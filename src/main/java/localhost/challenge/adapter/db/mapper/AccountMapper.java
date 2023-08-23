package localhost.challenge.adapter.db.mapper;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import localhost.challenge.adapter.db.entity.AccountEntity;
import localhost.challenge.domain.Account;
import org.javamoney.moneta.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface AccountMapper {

  @Mapping(target = "amount", source = "entity")
  Account toAccount(AccountEntity entity);

  default MonetaryAmount fromEntity(AccountEntity account) {
    return Money.of(account.getBalance(), Monetary.getCurrency(account.getCurrency()));
  }
}
