package localhost.challenge.adapter.rest.mapper;

import localhost.challenge.adapter.rest.dto.CreateTransactionDTO;
import localhost.challenge.domain.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface TransactionDTOMapper extends BalanceToMonetaryMapper {

  @Mapping(source = "txId", target = "transactionId")
  @Mapping(target = "amount", source = "amount", qualifiedBy = BalanceToMonetary.class)
  Transaction mapToDomain(CreateTransactionDTO dto);
}
