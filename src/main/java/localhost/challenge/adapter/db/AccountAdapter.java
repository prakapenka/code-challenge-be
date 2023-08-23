package localhost.challenge.adapter.db;

import java.math.BigDecimal;
import java.util.Optional;
import localhost.challenge.adapter.db.entity.AccountEntity;
import localhost.challenge.adapter.db.entity.AccountRepository;
import localhost.challenge.adapter.db.mapper.AccountMapper;
import localhost.challenge.domain.Account;
import localhost.challenge.domain.exception.AccountCreationException;
import localhost.challenge.service.port.CreateAccount;
import localhost.challenge.service.port.GetAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountAdapter implements GetAccount, CreateAccount {

  private final AccountRepository repository;

  private final AccountMapper mapper;

  @Override
  public Optional<Account> getAccount(String id) {
    return repository.findByAccountId(id).map(mapper::toAccount);
  }

  @Override
  public Account createAccount(Account account) throws AccountCreationException {
    var newEntity = new AccountEntity();
    newEntity.setBalance(account.amount().getNumber().numberValue(BigDecimal.class));
    newEntity.setCurrency(account.amount().getCurrency().getCurrencyCode());
    newEntity.setAccountId(account.accountId());

    try {
      var saved = repository.save(newEntity);
      return mapper.toAccount(saved);
    } catch (DataIntegrityViolationException cve) {
      throw new AccountCreationException(
          "unable to create account id: " + account.accountId(), cve);
    }
  }
}
