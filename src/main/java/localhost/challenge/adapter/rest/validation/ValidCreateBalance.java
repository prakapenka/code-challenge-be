package localhost.challenge.adapter.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = CreateBalanceValidator.class)
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCreateBalance {
  String message() default "Invalid create balance value";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
