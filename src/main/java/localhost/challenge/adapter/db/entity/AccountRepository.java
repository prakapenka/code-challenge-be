package localhost.challenge.adapter.db.entity;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface AccountRepository extends CrudRepository<AccountEntity, Long> {

  Optional<AccountEntity> findByAccountId(String accountId);
}
