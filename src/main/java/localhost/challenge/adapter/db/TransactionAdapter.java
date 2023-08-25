package localhost.challenge.adapter.db;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Objects;
import localhost.challenge.adapter.db.entity.AccountEntity;
import localhost.challenge.adapter.db.entity.AccountRepository;
import localhost.challenge.adapter.db.entity.TransactionEntity;
import localhost.challenge.adapter.db.entity.TransactionRepository;
import localhost.challenge.domain.Transaction;
import localhost.challenge.domain.exception.TransactionBalanceException;
import localhost.challenge.service.port.CreateTransaction;
import localhost.challenge.service.port.IsBalanceLimit;
import lombok.RequiredArgsConstructor;
import org.javamoney.moneta.Money;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionAdapter implements CreateTransaction {

  private final AccountRepository accountRepository;

  private final TransactionRepository transactionRepository;

  private final IsBalanceLimit isBalanceLimit;

  @Override
  @Transactional
  public void performTransaction(Transaction transaction) {

    final var accountPair = getAccountPair(transaction.from(), transaction.to());
    final var moneyFrom = Money.of(accountPair.from.getBalance(), accountPair.from.getCurrency());
    final var moneyTo = Money.of(accountPair.to.getBalance(), accountPair.to.getCurrency());

    if (!Objects.equals(moneyFrom.getCurrency(), moneyTo.getCurrency())) {
      throw new TransactionBalanceException("cross currency transactions not supported");
    }

    final var amount = transaction.amount();
    if (moneyFrom.isLessThan(transaction.amount())) {
      throw new TransactionBalanceException("from account: not enough balance");
    }

    final var fromNewBalance = moneyFrom.subtract(amount).getNumber().numberValue(BigDecimal.class);
    final var toNewBalance = moneyTo.add(amount).getNumber().numberValue(BigDecimal.class);

    // check resulted amount is too big to allow transfers
    if (isBalanceLimit.isRejected(toNewBalance)) {
      var message =
          MessageFormat.format(
              "Unable to process. Account {0}, amount rejected: {1}", accountPair.to, toNewBalance);
      throw new TransactionBalanceException(message);
    }

    accountPair.from.setBalance(fromNewBalance);
    accountPair.to.setBalance(toNewBalance);

    TransactionEntity transactionEntity = new TransactionEntity();
    transactionEntity.setTransactionId(transaction.transactionId());
    transactionEntity.setFrom(accountPair.from);
    transactionEntity.setTo(accountPair.to);
    transactionEntity.setAmount(amount.getNumber().numberValue(BigDecimal.class));
    transactionEntity.setCurrency(amount.getCurrency().getCurrencyCode());

    try {
      transactionRepository.save(transactionEntity);
    } catch (DataIntegrityViolationException dve) {
      // transaction id is not unique?
      throw new TransactionBalanceException("Unable to process transaction", dve);
    }
  }

  private record AccountPair(AccountEntity from, AccountEntity to) {}

  private AccountPair getAccountPair(String fromId, String toId) {
    var fromOp = accountRepository.findByAccountId(fromId);
    if (fromOp.isEmpty()) {
      throw new TransactionBalanceException("from account not found");
    }
    var toOp = accountRepository.findByAccountId(toId);
    if (toOp.isEmpty()) {
      throw new TransactionBalanceException("to account not found");
    }
    return new AccountPair(fromOp.get(), toOp.get());
  }
}
