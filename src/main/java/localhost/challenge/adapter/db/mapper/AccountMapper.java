package localhost.challenge.adapter.db.mapper;

import localhost.challenge.adapter.db.entity.AccountEntity;
import localhost.challenge.domain.Account;
import org.mapstruct.Mapper;

@Mapper
public interface AccountMapper {

  Account toAccount(AccountEntity entity);
}
