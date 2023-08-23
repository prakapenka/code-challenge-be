package localhost.challenge.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import java.io.IOException;
import javax.money.CurrencyUnit;
import javax.money.Monetary;

public final class CurrencyUnitDeserializer extends JsonDeserializer<CurrencyUnit> {

  @Override
  public Object deserializeWithType(
      final JsonParser parser,
      final DeserializationContext context,
      final TypeDeserializer deserializer)
      throws IOException {
    return deserialize(parser, context);
  }

  @Override
  public CurrencyUnit deserialize(final JsonParser parser, final DeserializationContext context)
      throws IOException {
    final String currencyCode = parser.getValueAsString();
    return Monetary.getCurrency(currencyCode);
  }
}
