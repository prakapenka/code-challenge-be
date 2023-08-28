package localhost.challenge.adapter.rest;

import jakarta.validation.Valid;
import localhost.challenge.adapter.rest.dto.CreateTransactionDTO;
import localhost.challenge.adapter.rest.mapper.TransactionDTOMapper;
import localhost.challenge.domain.Transaction;
import localhost.challenge.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TransactionController {

  private final TransactionService transactionService;
  private final TransactionDTOMapper mapper;

  @PostMapping
  void createTransaction(@RequestBody @Valid CreateTransactionDTO transactionDTO) {
    Transaction transaction = mapper.mapToDomain(transactionDTO);
    transactionService.createTransaction(transaction);
  }
}
