package localhost.challenge.adapter.db.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import lombok.Data;

@Entity(name = "account")
@Data
public class AccountEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private String accountId;

  private BigDecimal balance;

  private String currency;

  @Version private long version;
}
