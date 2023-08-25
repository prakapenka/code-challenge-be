package localhost.challenge.it;

import static localhost.challenge.util.LoadFileUtil.loadJsonFileAsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;
import localhost.challenge.it.util.TestContainers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
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

  private static Stream<Arguments> provideValidAccountCreationRequests() {
    String[][] files = {
      {"create_account_valid_1.json", "test_id_1.com", "0.0", "EUR"},
      {"create_account_valid_2.json", "test_id_2", "0.01", "EUR"},
      {"create_account_valid_3.json", "a", "1.0", "EUR"},
      {"create_account_valid_4.json", "ab", "1.0", "EUR"},
      {"create_account_valid_5.json", "fraction_99", "0.99", "EUR"},
      {"create_account_valid_6.json", "fraction_10", "0.1", "EUR"}
    };
    final String path = "classpath:it/create_account/";

    return Arrays.stream(files)
        .peek(f -> f[0] = path + f[0])
        .peek(
            f -> {
              try {
                f[0] = loadJsonFileAsString(f[0]);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
        .map(f -> Arguments.of(f[0], f[1], f[2], f[3]));
  }

  @ParameterizedTest
  @MethodSource("provideInvalidAccountCreationRequests")
  void testRejectInvalidAccountCreation(String json) throws Exception {
    this.mockMvc
        .perform(put("/").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isBadRequest());
  }

  private static Stream<Arguments> provideInvalidAccountCreationRequests() {
    String[] files = {
      "create_account_invalid_0.json",
      "create_account_invalid_1.json",
      "create_account_invalid_2.json",
      "create_account_invalid_3.json",
      "create_account_invalid_4.json",
      "create_account_invalid_5.json"
    };
    final String path = "classpath:it/create_account/";
    return Arrays.stream(files)
        .map(f -> path + f)
        .map(
            f -> {
              try {
                return loadJsonFileAsString(f);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
        .map(Arguments::of);
  }
}
