package localhost.challenge.adapter.db.entity;

import io.hypersistence.utils.hibernate.type.money.MonetaryAmountType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import javax.money.MonetaryAmount;
import lombok.Data;
import org.hibernate.annotations.CompositeType;

@Entity(name = "account")
@Data
public class AccountEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private String accountId;

  @AttributeOverride(name = "amount", column = @Column(name = "balance"))
  @AttributeOverride(name = "currency", column = @Column(name = "currency"))
  @CompositeType(MonetaryAmountType.class)
  MonetaryAmount amount;

  @Version private long version;
}
