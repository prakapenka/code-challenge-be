package localhost.challenge.adapter.rest.mapper;

import localhost.challenge.adapter.rest.dto.CreateTransactionDTO;
import localhost.challenge.domain.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface TransactionDTOMapper {

  @Mapping(source = "txId", target = "transactionId")
  Transaction mapToDomain(CreateTransactionDTO dto);
}
