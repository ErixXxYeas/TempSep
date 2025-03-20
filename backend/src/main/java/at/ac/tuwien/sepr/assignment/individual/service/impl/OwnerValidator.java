package at.ac.tuwien.sepr.assignment.individual.service.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * Validator for horse-related operations, ensuring that all horse data meets the required constraints.
 */
@Component
public class OwnerValidator {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Validates an Owner before Creating, ensuring all fields meet constraints and checking for conflicts.
   *
   * @param owner the {@link HorseUpdateDto} to validate
   * @throws ValidationException if validation fails
   * @throws ConflictException   if conflicts with existing data are detected
   */
  public void validateForCreate(OwnerCreateDto owner) throws ValidationException {
    LOG.trace("validateForCreate({})", owner);
    List<String> validationErrors = new ArrayList<>();

    if (owner.firstName() == null){
      validationErrors.add("Owner first name is not given");
    }
    if (owner.lastName() == null){
      validationErrors.add("Owner last name is not given");
    }

    if (owner.firstName() != null && owner.lastName() != null){
      if (owner.firstName().length() >= 255 || owner.lastName().length() >= 255){
        validationErrors.add("Owner name it too long");
      }
    }

    if (owner.description() != null) {
      if (owner.description().isBlank()) {
        validationErrors.add("Owner description is given but blank");
      }
      if (owner.description().length() > 4095) {
        validationErrors.add("Owner description too long: longer than 4095 characters");
      }
    }

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of Owner for Creation failed", validationErrors);
    }

  }

}
