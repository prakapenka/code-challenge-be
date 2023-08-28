package localhost.challenge.domain;

import javax.money.MonetaryAmount;

public record Transaction(String transactionId, String from, String to, MonetaryAmount amount) {}
