package localhost.challenge.adapter.db

import localhost.challenge.adapter.db.entity.AccountEntity
import localhost.challenge.adapter.db.entity.AccountRepository
import localhost.challenge.adapter.db.entity.TransactionEntity
import localhost.challenge.adapter.db.entity.TransactionRepository
import localhost.challenge.domain.Transaction
import localhost.challenge.domain.exception.TransactionBalanceException
import localhost.challenge.service.port.IsBalanceLimit
import org.javamoney.moneta.Money
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable
import org.mockito.ArgumentMatchers.*
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.util.*
import javax.money.Monetary
import javax.money.MonetaryAmount

@ExtendWith(MockitoExtension::class)
internal class TransactionAdapterTest {
    @Mock
    private val accountRepository: AccountRepository? = null

    @Mock
    private val transactionRepository: TransactionRepository? = null

    @Mock
    private val isBalanceLimit: IsBalanceLimit? = null
    private var transactionAdapter: TransactionAdapter? = null

    @BeforeEach
    fun beforeEach() {
        transactionAdapter = TransactionAdapter(accountRepository, transactionRepository, isBalanceLimit)
    }

    @Test
    @DisplayName("Test can process simple transaction")
    fun testProcessSimpleTransaction() {
        val transaction = getTestTransaction(10.01)
        val from = getAccount(20.01, "from")
        val to = getAccount(10.01, "to")
        `when`(accountRepository!!.findByAccountId(eq("from"))).thenReturn(Optional.of(from))
        `when`(accountRepository.findByAccountId(eq("to"))).thenReturn(Optional.of(to))
        `when`(isBalanceLimit!!.isRejected(any(MonetaryAmount::class.java))).thenReturn(false)

        transactionAdapter!!.performTransaction(transaction)
        assertBigDecimalEquals(
                BigDecimal.valueOf(20.02), to.amount.number.numberValue(BigDecimal::class.java))
        assertBigDecimalEquals(
                BigDecimal.valueOf(10), from.amount.number.numberValue(BigDecimal::class.java))
        verify(transactionRepository)!!
                .save(
                        assertArg { t: TransactionEntity ->
                            assertAll(
                                    Executable { Assertions.assertEquals("someTxId", t.transactionId) },
                                    Executable { Assertions.assertEquals(from, t.from) },
                                    Executable { Assertions.assertEquals(to, t.to) },
                                    Executable {
                                        assertBigDecimalEquals(
                                                BigDecimal.valueOf(10.01),
                                                t.amount.number.numberValue(BigDecimal::class.java))
                                    })
                        })
    }

    @Test
    @DisplayName("Test full amount withdrawn from 'from' account")
    fun testWholeAmountFromTransferred() {
        val transaction = getTestTransaction(10.01)
        val from = getAccount(10.01, "from")
        val to = getAccount(0.0, "to")
        `when`(accountRepository!!.findByAccountId(eq("from"))).thenReturn(Optional.of(from))
        `when`(accountRepository.findByAccountId(eq("to"))).thenReturn(Optional.of(to))
        `when`(isBalanceLimit!!.isRejected(any(MonetaryAmount::class.java))).thenReturn(false)

        transactionAdapter!!.performTransaction(transaction)

        assertBigDecimalEquals(
                BigDecimal.valueOf(0), from.amount.number.numberValue(BigDecimal::class.java))
        assertBigDecimalEquals(
                BigDecimal.valueOf(10.01), to.amount.number.numberValue(BigDecimal::class.java))
    }

    @Test
    @DisplayName("Test not enough money 'from' account")
    fun testNotEnoughBalanceFrom() {
        val transaction = getTestTransaction(10.01)
        val from = getAccount(10.00, "from")
        val to = getAccount(0.0, "to")
        `when`(accountRepository!!.findByAccountId(eq("from"))).thenReturn(Optional.of(from))
        `when`(accountRepository.findByAccountId(eq("to"))).thenReturn(Optional.of(to))

        Assertions.assertThrows(
                TransactionBalanceException::class.java
        ) { transactionAdapter!!.performTransaction(transaction) }

        verify(isBalanceLimit, Mockito.never())!!.isRejected(any(MonetaryAmount::class.java))
    }

    @Test
    @DisplayName("Test from account not found")
    fun testFromAccountNotFound() {
        val transaction = getTestTransaction(10.01)
        `when`(accountRepository!!.findByAccountId(eq("from"))).thenReturn(Optional.empty())

        Assertions.assertThrows(
                TransactionBalanceException::class.java
        ) { transactionAdapter!!.performTransaction(transaction) }

        verify(isBalanceLimit, Mockito.never())!!.isRejected(any(MonetaryAmount::class.java))
        // if from account not found we do not search for to account
        verify(accountRepository, Mockito.never()).findByAccountId(eq("to"))
    }

    @Test
    @DisplayName("Test to account not found")
    fun testToAccountNotFound() {
        val transaction = getTestTransaction(10.01)
        val from = getAccount(10.00, "from")
        `when`(accountRepository!!.findByAccountId(eq("from"))).thenReturn(Optional.of(from))
        `when`(accountRepository.findByAccountId(eq("to"))).thenReturn(Optional.empty())

        Assertions.assertThrows(
                TransactionBalanceException::class.java
        ) { transactionAdapter!!.performTransaction(transaction) }

        verify(isBalanceLimit, Mockito.never())!!.isRejected(any(MonetaryAmount::class.java))
    }

    @Test
    @DisplayName("Reject cross currencies transactions")
    fun testAccountDifferentCurrenciesReject() {
        val transaction = getTestTransaction(10.01)
        val from = getAccountWithCurrency(10.00, "EUR", "from")
        val to = getAccountWithCurrency(0.0, "USD", "to")
        `when`(accountRepository!!.findByAccountId(eq("from"))).thenReturn(Optional.of(from))
        `when`(accountRepository.findByAccountId(eq("to"))).thenReturn(Optional.of(to))

        Assertions.assertThrows(
                TransactionBalanceException::class.java
        ) { transactionAdapter!!.performTransaction(transaction) }

        verify(isBalanceLimit, Mockito.never())!!.isRejected(any(MonetaryAmount::class.java))
    }

    @Test
    @DisplayName("Test transaction rejected by balance limit service")
    fun testTransactionRejectedByIsBalanceLimitService() {
        val transaction = getTestTransaction(10.01)
        val from = getAccount(10.01, "from")
        val to = getAccount(0.0, "to")
        `when`(accountRepository!!.findByAccountId(eq("from"))).thenReturn(Optional.of(from))
        `when`(accountRepository.findByAccountId(eq("to"))).thenReturn(Optional.of(to))

        // explicitly reject transaction
        `when`(isBalanceLimit!!.isRejected(any(MonetaryAmount::class.java))).thenReturn(true)
        Assertions.assertThrows(
                TransactionBalanceException::class.java
        ) { transactionAdapter!!.performTransaction(transaction) }

        val expectedMoney = Money.of(BigDecimal.valueOf(10.01), Monetary.getCurrency("EUR"))
        verify(isBalanceLimit).isRejected(eq(expectedMoney))
        verify(transactionRepository, Mockito.never())!!.save(any())
    }

    private fun assertBigDecimalEquals(expected: BigDecimal, actual: BigDecimal) {
        Assertions.assertEquals(expected.toPlainString(), actual.toPlainString())
    }

    private fun getTestTransaction(amount: Double): Transaction {
        return Transaction(
                "someTxId", "from", "to", Money.of(BigDecimal.valueOf(amount), Monetary.getCurrency("EUR")))
    }

    private fun getAccount(amount: Double, id: String): AccountEntity {
        return getAccountWithCurrency(amount, "EUR", id)
    }

    private fun getAccountWithCurrency(amount: Double, currency: String, id: String): AccountEntity {
        val ac = AccountEntity()
        ac.amount = Money.of(BigDecimal.valueOf(amount), currency)
        ac.accountId = id
        return ac
    }
}
