package localhost.challenge.adapter.db;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import javax.money.Monetary;
import localhost.challenge.adapter.db.entity.AccountEntity;
import localhost.challenge.adapter.db.entity.AccountRepository;
import localhost.challenge.adapter.db.entity.TransactionRepository;
import localhost.challenge.domain.Transaction;
import localhost.challenge.domain.exception.TransactionBalanceException;
import localhost.challenge.service.port.IsBalanceLimit;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class})
class TransactionAdapterTest {
  @Mock private AccountRepository accountRepository;
  @Mock private TransactionRepository transactionRepository;
  @Mock private IsBalanceLimit isBalanceLimit;

  private TransactionAdapter transactionAdapter;

  @BeforeEach
  void beforeEach() {
    transactionAdapter =
        new TransactionAdapter(accountRepository, transactionRepository, isBalanceLimit);
  }

  @Test
  @DisplayName("Test can process simple transaction")
  void testProcessSimpleTransaction() {
    var transaction = getTestTransaction(10.01);
    final var from = getAccount(20.01, "from");
    final var to = getAccount(10.01, "to");

    when(accountRepository.findByAccountId(eq("from"))).thenReturn(Optional.of(from));
    when(accountRepository.findByAccountId(eq("to"))).thenReturn(Optional.of(to));
    when(isBalanceLimit.isRejected(any(BigDecimal.class))).thenReturn(false);

    transactionAdapter.performTransaction(transaction);

    assertBigDecimalEquals(BigDecimal.valueOf(20.02), to.getBalance());
    assertBigDecimalEquals(BigDecimal.valueOf(10), from.getBalance());

    verify(transactionRepository)
        .save(
            assertArg(
                t ->
                    assertAll(
                        () -> assertEquals("someTxId", t.getTransactionId()),
                        () -> assertEquals(from, t.getFrom()),
                        () -> assertEquals(to, t.getTo()),
                        () -> assertBigDecimalEquals(BigDecimal.valueOf(10.01), t.getAmount()))));
  }

  @Test
  @DisplayName("Test full amount withdrawn from 'from' account")
  void testWholeAmountFromTransferred() {
    var transaction = getTestTransaction(10.01);
    final var from = getAccount(10.01, "from");
    final var to = getAccount(0, "to");
    when(accountRepository.findByAccountId(eq("from"))).thenReturn(Optional.of(from));
    when(accountRepository.findByAccountId(eq("to"))).thenReturn(Optional.of(to));
    when(isBalanceLimit.isRejected(any(BigDecimal.class))).thenReturn(false);

    transactionAdapter.performTransaction(transaction);

    assertBigDecimalEquals(BigDecimal.valueOf(0), from.getBalance());
    assertBigDecimalEquals(BigDecimal.valueOf(10.01), to.getBalance());
  }

  @Test
  @DisplayName("Test not enough money 'from' account")
  void testNotEnoughBalanceFrom() {
    var transaction = getTestTransaction(10.01);
    final var from = getAccount(10.00, "from");
    final var to = getAccount(0, "to");
    when(accountRepository.findByAccountId(eq("from"))).thenReturn(Optional.of(from));
    when(accountRepository.findByAccountId(eq("to"))).thenReturn(Optional.of(to));

    assertThrows(
        TransactionBalanceException.class,
        () -> transactionAdapter.performTransaction(transaction));

    verify(isBalanceLimit, never()).isRejected(any());
  }

  @Test
  @DisplayName("Test from account not found")
  void testFromAccountNotFound() {
    var transaction = getTestTransaction(10.01);
    when(accountRepository.findByAccountId(eq("from"))).thenReturn(Optional.empty());

    assertThrows(
        TransactionBalanceException.class,
        () -> transactionAdapter.performTransaction(transaction));

    verify(isBalanceLimit, never()).isRejected(any());
    // if from account not found we do not search for to account
    verify(accountRepository, never()).findByAccountId(eq("to"));
  }

  @Test
  @DisplayName("Test to account not found")
  void testToAccountNotFound() {
    var transaction = getTestTransaction(10.01);
    final var from = getAccount(10.00, "from");
    when(accountRepository.findByAccountId(eq("from"))).thenReturn(Optional.of(from));
    when(accountRepository.findByAccountId(eq("to"))).thenReturn(Optional.empty());

    assertThrows(
        TransactionBalanceException.class,
        () -> transactionAdapter.performTransaction(transaction));

    verify(isBalanceLimit, never()).isRejected(any());
  }

  @Test
  void testAccountDifferentCurrenciesReject() {
    var transaction = getTestTransaction(10.01);
    final var from = getAccountWithCurrency(10.00, "EUR", "from");
    final var to = getAccountWithCurrency(0, "USD", "to");
    when(accountRepository.findByAccountId(eq("from"))).thenReturn(Optional.of(from));
    when(accountRepository.findByAccountId(eq("to"))).thenReturn(Optional.of(to));

    assertThrows(
        TransactionBalanceException.class,
        () -> transactionAdapter.performTransaction(transaction));

    verify(isBalanceLimit, never()).isRejected(any());
  }

  @Test
  @DisplayName("Test transaction rejected by balance limit service")
  void testTransactionRejectedByIsBalanceLimitService() {
    var transaction = getTestTransaction(10.01);
    final var from = getAccount(10.01, "from");
    final var to = getAccount(0, "to");
    when(accountRepository.findByAccountId(eq("from"))).thenReturn(Optional.of(from));
    when(accountRepository.findByAccountId(eq("to"))).thenReturn(Optional.of(to));

    // explicitly reject transaction
    when(isBalanceLimit.isRejected(any(BigDecimal.class))).thenReturn(true);

    assertThrows(
        TransactionBalanceException.class,
        () -> transactionAdapter.performTransaction(transaction));

    verify(isBalanceLimit).isRejected(eq(BigDecimal.valueOf(10.01)));
    verify(transactionRepository, never()).save(any());
  }

  private void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
    assertEquals(expected.toPlainString(), actual.toPlainString());
  }

  private Transaction getTestTransaction(double amount) {
    var bigAmount = BigDecimal.valueOf(amount);
    return new Transaction(
        "someTxId", "from", "to", Money.of(bigAmount, Monetary.getCurrency("EUR")));
  }

  private AccountEntity getAccount(double amount, String id) {
    return getAccountWithCurrency(amount, "EUR", id);
  }

  private AccountEntity getAccountWithCurrency(double amount, String currency, String id) {
    AccountEntity ac = new AccountEntity();
    ac.setBalance(BigDecimal.valueOf(amount));
    ac.setCurrency(currency);
    ac.setAccountId(id);
    return ac;
  }
}
