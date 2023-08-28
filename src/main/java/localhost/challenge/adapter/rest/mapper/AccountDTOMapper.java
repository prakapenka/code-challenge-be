package localhost.challenge.adapter.rest.mapper;

import localhost.challenge.adapter.rest.dto.AccountDTO;
import localhost.challenge.adapter.rest.dto.CreateAccountDTO;
import localhost.challenge.domain.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface AccountDTOMapper {

  @Mapping(source = "balance", target = "amount")
  Account mapToDomain(CreateAccountDTO dto);

  @Mapping(source = "amount", target = "balance")
  AccountDTO mapToDTO(Account account);
}
