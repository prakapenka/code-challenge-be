package localhost.challenge.it

import localhost.challenge.it.util.TestContainers
import localhost.challenge.util.LoadFileUtil.loadJsonFileAsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.Sql.ExecutionPhase
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
class TransactionControllerTest : TestContainers() {
    @Autowired
    private val mockMvc: MockMvc? = null
    @Sql(scripts = ["/it/create_2_accounts.sql"])
    @Sql(scripts = ["/it/truncate_all.sql"], executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    @Throws(Exception::class)
    fun testCanSubmitTransaction() {
        val transactionJson = loadJsonFileAsString("classpath:it/create_transaction/create_simple_transaction.json")

        // check account balance
        mockMvc!!
                .perform(get("/a1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance.amount").value("1.01"))
        mockMvc
                .perform(get("/a2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance.amount").value("1.01"))

        // perform transaction
        mockMvc
                .perform(MockMvcRequestBuilders.post("/").contentType(APPLICATION_JSON).content(transactionJson))
                .andExpect(status().isOk())

        // check updated balance
        mockMvc
                .perform(get("/a1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance.amount").value("1.0"))
        mockMvc
                .perform(get("/a2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance.amount").value("1.02"))
    }
}
