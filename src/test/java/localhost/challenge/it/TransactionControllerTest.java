package localhost.challenge.it;

import static localhost.challenge.util.LoadFileUtil.loadJsonFileAsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import localhost.challenge.it.util.TestContainers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
public class TransactionControllerTest extends TestContainers {

  @Autowired private MockMvc mockMvc;

  @Sql(scripts = {"/it/create_2_accounts.sql"})
  @Sql(
      scripts = {"/it/truncate_all.sql"},
      executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
  @Test
  public void testCanSubmitTransaction() throws Exception {
    final String transactionJson =
        loadJsonFileAsString("classpath:it/create_transaction/create_simple_transaction.json");

    // check account balance
    this.mockMvc
        .perform(get("/a1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.balance.amount").value("1.01"));
    this.mockMvc
        .perform(get("/a2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.balance.amount").value("1.01"));

    // perform transaction
    this.mockMvc
        .perform(post("/").contentType(MediaType.APPLICATION_JSON).content(transactionJson))
        .andExpect(status().isOk());

    // check updated balance
    this.mockMvc
        .perform(get("/a1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.balance.amount").value("1"));
    this.mockMvc
        .perform(get("/a2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.balance.amount").value("1.02"));
  }
}
