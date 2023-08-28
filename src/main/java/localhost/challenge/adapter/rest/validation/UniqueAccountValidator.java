package localhost.challenge.adapter.rest.validation;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import localhost.challenge.adapter.rest.dto.CreateAccountDTO;
import localhost.challenge.service.port.IsAccountExists;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UniqueAccountValidator
    implements ConstraintValidator<UniqueNewAccount, CreateAccountDTO> {

  private final IsAccountExists getAccountPort;

  @Override
  public boolean isValid(CreateAccountDTO dto, ConstraintValidatorContext context) {
    final String id = dto.accountId();
    if (isEmpty(id)) {
      return false;
    }
    final var existed = getAccountPort.accountExists(id);
    if (existed) {
      context
          .buildConstraintViolationWithTemplate("conflict: account exists")
          .addPropertyNode("accountId")
          .addConstraintViolation();
      return false;
    }
    return true;
  }
}
