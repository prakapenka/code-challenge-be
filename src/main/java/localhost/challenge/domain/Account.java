package localhost.challenge.domain;

import javax.money.MonetaryAmount;

public record Account(String accountId, MonetaryAmount amount) {}
