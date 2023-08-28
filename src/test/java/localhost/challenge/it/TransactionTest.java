package localhost.challenge.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import javax.money.Monetary;
import localhost.challenge.adapter.db.TransactionAdapter;
import localhost.challenge.adapter.db.entity.AccountEntity;
import localhost.challenge.adapter.db.entity.AccountRepository;
import localhost.challenge.adapter.db.entity.TransactionRepository;
import localhost.challenge.domain.Transaction;
import localhost.challenge.domain.exception.TransactionBalanceException;
import localhost.challenge.it.util.TestContainers;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
class TransactionTest extends TestContainers {

  @Autowired private TransactionRepository transactionRepository;

  @Autowired private TransactionAdapter transactionAdapter;

  @Autowired private AccountRepository accountRepository;

  @Sql(scripts = {"/it/create_account_and_transaction.sql"})
  @Sql(
      scripts = {"/it/truncate_all.sql"},
      executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
  @Test
  void testCanReadTransactionFromDB() {
    var op = transactionRepository.findByTransactionId("test-transaction");

    assertTrue(op.isPresent());
    var transaction = op.get();

    assertEquals("test-transaction", transaction.getTransactionId());

    var fromAccount = transaction.getFrom();
    var toAccount = transaction.getTo();

    assertEquals("uuid_1", fromAccount.getAccountId());
    assertEquals("uuid_2", toAccount.getAccountId());
  }

  @Sql(scripts = {"/it/create_2_accounts.sql"})
  @Sql(
      scripts = {"/it/truncate_all.sql"},
      executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
  @Test
  void testCanCreateTransactionBetweenAccounts() {
    var actualFromBalance_1 = getBalanceForAccount("a1");
    assertEquals(BigDecimal.valueOf(1.01), actualFromBalance_1);

    var actualToBalance_1 = getBalanceForAccount("a2");
    assertEquals(BigDecimal.valueOf(1.01), actualToBalance_1);

    var amount = BigDecimal.valueOf(0.59);
    Transaction transaction =
        new Transaction("testId", "a1", "a2", Money.of(amount, Monetary.getCurrency("EUR")));

    transactionAdapter.performTransaction(transaction);

    var actualFromBalance_2 = getBalanceForAccount("a1");
    assertEquals(BigDecimal.valueOf(0.42), actualFromBalance_2);

    var actualToBalance_2 = getBalanceForAccount("a2");
    assertEquals(BigDecimal.valueOf(1.6), actualToBalance_2);
  }

  @Sql(scripts = {"/it/create_account_and_transaction.sql"})
  @Sql(
      scripts = {"/it/truncate_all.sql"},
      executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
  @Test
  void testRollbackIfAccountIdNotUnique() {
    var actualFromBalance_1 = getBalanceForAccount("uuid_1");
    assertEquals(BigDecimal.valueOf(100), actualFromBalance_1);
    var actualToBalance_1 = getBalanceForAccount("uuid_2");
    assertEquals(BigDecimal.valueOf(200), actualToBalance_1);

    assertTrue(transactionRepository.findByTransactionId("test-transaction").isPresent());

    var amount = BigDecimal.valueOf(50);
    Transaction transaction =
        new Transaction(
            "test-transaction", "uuid_1", "uuid_2", Money.of(amount, Monetary.getCurrency("EUR")));

    assertThrows(
        TransactionBalanceException.class,
        () -> transactionAdapter.performTransaction(transaction));

    var actualFromBalance_2 = getBalanceForAccount("uuid_1");
    assertEquals(BigDecimal.valueOf(100), actualFromBalance_2);
    var actualToBalance_2 = getBalanceForAccount("uuid_2");
    assertEquals(BigDecimal.valueOf(200), actualToBalance_2);
  }

  private BigDecimal getBalanceForAccount(String accountId) {
    return accountRepository
        .findByAccountId(accountId)
        .map(AccountEntity::getAmount)
        .map(ma -> ma.getNumber().numberValue(BigDecimal.class))
        .orElseThrow();
  }
}
