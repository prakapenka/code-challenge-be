package localhost.challenge.it;

import static localhost.challenge.util.LoadFileUtil.loadJsonFileAsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest extends TestContainers {

  @Autowired private MockMvc mockMvc;

  @Sql(scripts = {"/it/create_account.sql"})
  @Sql(
      scripts = {"/it/truncate_all.sql"},
      executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
  @Test
  void testCanReadAccount() throws Exception {
    final var expectedId = "ece28caa-40e6-11ee-af27-03d1e77c1156";
    this.mockMvc
        .perform(get("/" + expectedId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accountId").value(expectedId))
        .andExpect(jsonPath("$.balance.amount").value("1.01"))
        .andExpect(jsonPath("$.balance.currencyUnit").value("EUR"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"süß", "t&", "OR 1=1;DROP TABLE ACCOUNT;", "***"})
  void testRejectInvalidAccountId(String invalidId) throws Exception {
    this.mockMvc.perform(get("/" + invalidId)).andExpect(status().isBadRequest());
  }

  @Test
  @Sql(scripts = {"/it/truncate_all.sql"})
  void testUnableToCreateDuplicateAccountId() throws Exception {
    final var json =
        loadJsonFileAsString("classpath:it/create_account/create_account_valid_1.json");

    // account created
    this.mockMvc
        .perform(put("/").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isOk());

    // account existed
    this.mockMvc
        .perform(put("/").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isBadRequest());
  }

  @ParameterizedTest
  @MethodSource("provideValidAccountCreationRequests")
  @Sql(
      scripts = {"/it/truncate_all.sql"},
      executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
  void testCanCreateAccount(
      String json, String expectedId, String expectedBalance, String expectedCurrency)
      throws Exception {
    this.mockMvc
        .perform(put("/").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accountId").value(expectedId))
        .andExpect(jsonPath("$.balance.amount").value(expectedBalance))
        .andExpect(jsonPath("$.balance.currencyUnit").value(expectedCurrency));

    this.mockMvc
        .perform(get("/" + expectedId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accountId").value(expectedId))
        .andExpect(jsonPath("$.balance.amount").value(expectedBalance))
        .andExpect(jsonPath("$.balance.currencyUnit").value(expectedCurrency));
  }

  private static Stream<Arguments> provideValidAccountCreationRequests() throws IOException {
    return Stream.of(
        Arguments.of(
            loadJsonFileAsString("classpath:it/create_account/create_account_valid_1.json"),
            "test_id_1.com",
            "0",
            "EUR"),
        Arguments.of(
            loadJsonFileAsString("classpath:it/create_account/create_account_valid_2.json"),
            "test_id_2",
            "0.01",
            "EUR"),
        Arguments.of(
            loadJsonFileAsString("classpath:it/create_account/create_account_valid_3.json"),
            "a",
            "1",
            "EUR"),
        Arguments.of(
            loadJsonFileAsString("classpath:it/create_account/create_account_valid_4.json"),
            "ab",
            "1",
            "EUR"),
        Arguments.of(
            loadJsonFileAsString("classpath:it/create_account/create_account_valid_5.json"),
            "fraction_99",
            "0.99",
            "EUR"),
        Arguments.of(
            loadJsonFileAsString("classpath:it/create_account/create_account_valid_6.json"),
            "fraction_10",
            "0.1",
            "EUR"));
  }

  @ParameterizedTest
  @MethodSource("provideInvalidAccountCreationRequests")
  void testRejectInvalidAccountCreation(String json) throws Exception {
    this.mockMvc
        .perform(put("/").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isBadRequest());
  }

  private static Stream<Arguments> provideInvalidAccountCreationRequests() throws IOException {
    return Stream.of(
        // invalid account id
        Arguments.of(
            loadJsonFileAsString("classpath:it/create_account/create_account_invalid_0.json")),
        Arguments.of(
            loadJsonFileAsString("classpath:it/create_account/create_account_invalid_1.json")),
        // amount less than 0
        Arguments.of(
            loadJsonFileAsString("classpath:it/create_account/create_account_invalid_2.json")),
        // currency is not allowed
        Arguments.of(
            loadJsonFileAsString("classpath:it/create_account/create_account_invalid_3.json")),
        // extra factions are not allowed
        Arguments.of(
            loadJsonFileAsString("classpath:it/create_account/create_account_invalid_4.json")),
        // unknown currency
        Arguments.of(
            loadJsonFileAsString("classpath:it/create_account/create_account_invalid_5.json")));
  }
}
