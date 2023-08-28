package localhost.challenge.adapter.db.entity;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface TransactionRepository extends CrudRepository<TransactionEntity, Long> {
  Optional<TransactionEntity> findByTransactionId(String id);
}
