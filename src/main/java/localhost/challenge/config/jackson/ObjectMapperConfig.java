package localhost.challenge.config.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import javax.money.CurrencyUnit;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapperConfig {

  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
    SimpleModule sm = new SimpleModule();
    sm.addSerializer(CurrencyUnit.class, new CurrencyUnitSerializer());
    sm.addDeserializer(CurrencyUnit.class, new CurrencyUnitDeserializer());
    return builder -> builder.modulesToInstall(sm);
  }
}
