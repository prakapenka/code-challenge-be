package localhost.challenge.adapter.db.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import java.math.BigDecimal;
import lombok.Data;

@Entity(name = "transaction")
@Data
public class TransactionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @OneToOne AccountEntity from;

  @OneToOne AccountEntity to;

  private BigDecimal amount;

  private String currency;

  private String transactionId;
}
