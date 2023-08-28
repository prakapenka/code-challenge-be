package localhost.challenge.adapter.rest.dto;

import javax.money.MonetaryAmount;

public record AccountDTO(String accountId, MonetaryAmount balance) {}
