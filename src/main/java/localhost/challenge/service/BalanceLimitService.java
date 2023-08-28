package localhost.challenge.service;

import java.math.BigDecimal;
import localhost.challenge.service.port.IsBalanceLimit;
import org.springframework.stereotype.Service;

@Service
public class BalanceLimitService implements IsBalanceLimit {

  BigDecimal MAX_LIMIT = BigDecimal.valueOf(1_000_000_000);

  @Override
  public BigDecimal getBalanceLimit() {
    return MAX_LIMIT;
  }
}
