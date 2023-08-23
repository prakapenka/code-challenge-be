package localhost.challenge.config.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import javax.money.CurrencyUnit;

public final class CurrencyUnitSerializer extends StdSerializer<CurrencyUnit> {
  CurrencyUnitSerializer() {
    super(CurrencyUnit.class);
  }

  @Override
  public void serialize(
      final CurrencyUnit value, final JsonGenerator generator, final SerializerProvider serializers)
      throws IOException {
    generator.writeString(value.getCurrencyCode());
  }

  @Override
  public void acceptJsonFormatVisitor(final JsonFormatVisitorWrapper visitor, final JavaType hint)
      throws JsonMappingException {
    visitor.expectStringFormat(hint);
  }
}
