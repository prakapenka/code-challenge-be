package localhost.challenge.service

import jakarta.persistence.OptimisticLockException
import localhost.challenge.config.retry.RetryConfig
import localhost.challenge.domain.Transaction
import localhost.challenge.domain.exception.TransactionBalanceException
import localhost.challenge.service.port.CreateTransaction
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import javax.money.MonetaryAmount

@ExtendWith(SpringExtension::class, MockitoExtension::class)
@ContextConfiguration(classes = [TransactionService::class, RetryConfig::class])
internal class TransactionServiceTest {
    @MockBean
    private val createTransactionMock: CreateTransaction? = null

    @Autowired
    private val service: TransactionService? = null

    private val transaction: Transaction = Transaction("testId", "testFrom", "testTo", Mockito.mock(MonetaryAmount::class.java))

    @Test
    fun testRetry2TimesOnOptimisticLockException() {
        doThrow(OptimisticLockException::class.java)
                .`when`(createTransactionMock)
                ?.performTransaction(ArgumentMatchers.any(Transaction::class.java))
        assertThrows(TransactionBalanceException::class.java) { service!!.createTransaction(transaction) }
        verify(createTransactionMock, times(2))!!.performTransaction(eq(transaction))
    }

    @Test
    fun testDoNotRetryOnOtherException() {
        doThrow(NullPointerException::class.java)
                .`when`(createTransactionMock)
                ?.performTransaction(ArgumentMatchers.any(Transaction::class.java))
        assertThrows(TransactionBalanceException::class.java) { service!!.createTransaction(transaction) }
        verify(createTransactionMock, times(1))!!.performTransaction(eq(transaction))
    }
}
