package localhost.challenge.it

import localhost.challenge.it.util.TestContainers
import localhost.challenge.util.LoadFileUtil.loadJsonFileAsString
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.Sql.ExecutionPhase
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*
import java.util.function.Function
import java.util.stream.Stream

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
internal class AccountControllerTest : TestContainers() {
    @Autowired
    private val mockMvc: MockMvc? = null

    @Sql(scripts = ["/it/create_account.sql"])
    @Sql(scripts = ["/it/truncate_all.sql"], executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    @Throws(Exception::class)
    fun testCanReadAccount() {
        val expectedId = "ece28caa-40e6-11ee-af27-03d1e77c1156"
        mockMvc!!
                .perform(get("/$expectedId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(expectedId))
                .andExpect(jsonPath("$.balance.amount").value("1.01"))
                .andExpect(jsonPath("$.balance.currencyUnit").value("EUR"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["süß", "t&", "OR 1=1;DROP TABLE ACCOUNT;", "***"])
    @Throws(Exception::class)
    fun testRejectInvalidAccountId(invalidId: String) {
        mockMvc!!.perform(get("/$invalidId")).andExpect(status().isBadRequest())
    }

    @Test
    @Sql(scripts = ["/it/truncate_all.sql"])
    @Throws(Exception::class)
    fun testUnableToCreateDuplicateAccountId() {
        val json = loadJsonFileAsString("classpath:it/create_account/create_account_valid_1.json")

        // account created
        mockMvc!!
                .perform(put("/").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())

        // account existed
        mockMvc
                .perform(put("/").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
    }

    @ParameterizedTest
    @MethodSource("provideValidAccountCreationRequests")
    @Sql(scripts = ["/it/truncate_all.sql"], executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    @Throws(Exception::class)
    fun testCanCreateAccount(
            json: String?, expectedId: String, expectedBalance: String?, expectedCurrency: String?) {
        mockMvc!!
                .perform(put("/").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(expectedId))
                .andExpect(jsonPath("$.balance.amount").value(expectedBalance))
                .andExpect(jsonPath("$.balance.currencyUnit").value(expectedCurrency))
        mockMvc
                .perform(get("/$expectedId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(expectedId))
                .andExpect(jsonPath("$.balance.amount").value(expectedBalance))
                .andExpect(jsonPath("$.balance.currencyUnit").value(expectedCurrency))
    }

    @ParameterizedTest
    @MethodSource("provideInvalidAccountCreationRequests")
    @Throws(Exception::class)
    fun testRejectInvalidAccountCreation(json: String?) {
        mockMvc!!
                .perform(put("/").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
    }

    companion object {
        @JvmStatic
        private fun provideValidAccountCreationRequests(): Stream<Arguments> {
            val files = arrayOf(
                    arrayOf("create_account_valid_1.json", "test_id_1.com", "0.0", "EUR"),
                    arrayOf("create_account_valid_2.json", "test_id_2", "0.01", "EUR"),
                    arrayOf("create_account_valid_3.json", "a", "1.0", "EUR"),
                    arrayOf("create_account_valid_4.json", "ab", "1.0", "EUR"),
                    arrayOf("create_account_valid_5.json", "fraction_99", "0.99", "EUR"),
                    arrayOf("create_account_valid_6.json", "fraction_10", "0.1", "EUR"))
            val path = "classpath:it/create_account/"
            return Arrays.stream(files)
                    .peek { f -> f[0] = path + f[0] }
                    .peek { f -> f[0] = loadJsonFileAsString(f[0]) }
                    .map { f -> Arguments.of(f[0], f[1], f[2], f[3]) }
        }
        @JvmStatic
        private fun provideInvalidAccountCreationRequests(): Stream<Arguments> {
            val files = arrayOf(
                    "create_account_invalid_0.json",
                    "create_account_invalid_1.json",
                    "create_account_invalid_2.json",
                    "create_account_invalid_3.json",
                    "create_account_invalid_4.json",
                    "create_account_invalid_5.json"
            )
            val path = "classpath:it/create_account/"
            return Arrays.stream(files)
                    .map { f -> path + f }
                    .map { f -> loadJsonFileAsString(f) }
                    .map { arguments -> Arguments.of(arguments) }
        }
    }
}
