package localhost.challenge.adapter.rest.mapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.money.MonetaryAmount;
import localhost.challenge.adapter.rest.dto.AccountBalanceDTO;
import org.javamoney.moneta.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Qualifier;

@Mapper
public interface BalanceToMonetaryMapper {

  @BalanceToMonetary
  default MonetaryAmount fromBalanceDTO(AccountBalanceDTO dto) {
    return Money.of(dto.amount(), dto.currencyUnit());
  }

  @Qualifier
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.CLASS)
  @interface BalanceToMonetary {}
}
