package localhost.challenge.config.jackson;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.jackson.datatype.money.MoneyModule;

@Configuration
public class ObjectMapperConfig {

  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
    var moneyModule = new MoneyModule().withCurrencyFieldName("currencyUnit");
    return builder -> builder.modulesToInstall(moneyModule);
  }
}
