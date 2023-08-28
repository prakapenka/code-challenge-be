package localhost.challenge.adapter.db.entity;

import io.hypersistence.utils.hibernate.type.money.MonetaryAmountType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import javax.money.MonetaryAmount;
import lombok.Data;
import org.hibernate.annotations.CompositeType;

@Entity(name = "transaction")
@Data
public class TransactionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @OneToOne AccountEntity from;

  @OneToOne AccountEntity to;

  @AttributeOverride(name = "amount", column = @Column(name = "amount"))
  @AttributeOverride(name = "currency", column = @Column(name = "currency"))
  @CompositeType(MonetaryAmountType.class)
  private MonetaryAmount amount;

  private String transactionId;
}
