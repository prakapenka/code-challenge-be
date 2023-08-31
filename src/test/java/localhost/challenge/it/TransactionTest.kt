package localhost.challenge.it

import localhost.challenge.adapter.db.TransactionAdapter
import localhost.challenge.adapter.db.entity.AccountRepository
import localhost.challenge.adapter.db.entity.TransactionRepository
import localhost.challenge.domain.Transaction
import localhost.challenge.domain.exception.TransactionBalanceException
import localhost.challenge.it.util.TestContainers
import org.javamoney.moneta.Money
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.Sql.ExecutionPhase
import java.math.BigDecimal
import javax.money.Monetary

@ActiveProfiles(profiles = ["default", "test"])
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
internal class TransactionTest : TestContainers() {
    @Autowired
    private val transactionRepository: TransactionRepository? = null

    @Autowired
    private val transactionAdapter: TransactionAdapter? = null

    @Autowired
    private val accountRepository: AccountRepository? = null

    @Sql(scripts = ["/it/create_account_and_transaction.sql"])
    @Sql(scripts = ["/it/truncate_all.sql"], executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    fun testCanReadTransactionFromDB() {
        val transaction = transactionRepository!!.findByTransactionId("test-transaction").orElseThrow()
        assertEquals("test-transaction", transaction.transactionId)
        assertEquals("uuid_1", transaction.from.accountId)
        assertEquals("uuid_2", transaction.to.accountId)
    }

    @Sql(scripts = ["/it/create_2_accounts.sql"])
    @Sql(scripts = ["/it/truncate_all.sql"], executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    fun testCanCreateTransactionBetweenAccounts() {

        assertEquals(BigDecimal.valueOf(1.01), getBalanceForAccount("a1"))
        assertEquals(BigDecimal.valueOf(1.01), getBalanceForAccount("a2"))

        val transaction = Transaction("testId", "a1", "a2",
                Money.of(BigDecimal.valueOf(0.59), Monetary.getCurrency("EUR")))
        transactionAdapter!!.performTransaction(transaction)

        assertEquals(BigDecimal.valueOf(0.42), getBalanceForAccount("a1"))
        assertEquals(BigDecimal.valueOf(1.6), getBalanceForAccount("a2"))
    }

    @Sql(scripts = ["/it/create_account_and_transaction.sql"])
    @Sql(scripts = ["/it/truncate_all.sql"], executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    fun testRollbackIfTransactionIdNotUnique() {

        assertEquals(BigDecimal.valueOf(100), getBalanceForAccount("uuid_1"))
        assertEquals(BigDecimal.valueOf(200), getBalanceForAccount("uuid_2"))

        assertTrue(transactionRepository!!.findByTransactionId("test-transaction").isPresent)

        val transaction = Transaction(
                "test-transaction", "uuid_1", "uuid_2", Money.of(BigDecimal.valueOf(50), Monetary.getCurrency("EUR")))
        assertThrows(TransactionBalanceException::class.java) { transactionAdapter!!.performTransaction(transaction) }

        assertEquals(BigDecimal.valueOf(100), getBalanceForAccount("uuid_1"))
        assertEquals(BigDecimal.valueOf(200), getBalanceForAccount("uuid_2"))
    }

    private fun getBalanceForAccount(accountId: String): BigDecimal {
        return accountRepository!!
                .findByAccountId(accountId)
                .map { obj -> obj.amount }
                .map { ma -> ma.number.numberValue(BigDecimal::class.java) }
                .orElseThrow()
    }
}
