package localhost.challenge.adapter.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.Optional;
import localhost.challenge.adapter.rest.dto.AccountDTO;
import localhost.challenge.adapter.rest.dto.CreateAccountDTO;
import localhost.challenge.adapter.rest.mapper.AccountDTOMapper;
import localhost.challenge.adapter.rest.validation.util.AccountValidationConst;
import localhost.challenge.service.port.CreateAccount;
import localhost.challenge.service.port.GetAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
public class AccountController {

  final GetAccount getAccountPort;
  final CreateAccount createAccountPort;

  final AccountDTOMapper mapper;

  @PutMapping
  public AccountDTO creteNewAccount(@RequestBody @Valid CreateAccountDTO createAccountDTO) {
    var created = createAccountPort.createAccount(mapper.mapToDomain(createAccountDTO));
    return mapper.mapToDTO(created);
  }

  @GetMapping(path = "/{id}")
  public Optional<AccountDTO> fetchAccountBalance(
      @PathVariable
          @Pattern(
              regexp = AccountValidationConst.ACCOUNT_ID_PATTERN,
              message = "Invalid account id")
          String id) {
    return getAccountPort.getAccount(id).map(mapper::mapToDTO);
  }
}
