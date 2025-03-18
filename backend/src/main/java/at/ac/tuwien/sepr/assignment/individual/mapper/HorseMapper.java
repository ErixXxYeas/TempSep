package at.ac.tuwien.sepr.assignment.individual.mapper;

import at.ac.tuwien.sepr.assignment.individual.dto.*;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.swing.text.html.parser.Entity;

/**
 * Mapper class responsible for converting {@link Horse} entities into various DTOs.
 */
@Component
public class HorseMapper {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Converts a {@link Horse} entity into a {@link HorseListDto}.
   * The given map of owners must contain the owner referenced by the horse.
   *
   * @param horse  the horse entity to convert
   * @param owners a map of horse owners by their ID
   * @return the converted {@link HorseListDto}
   */
  public HorseListDto entityToListDto(Horse horse, Map<Long, OwnerDto> owners, HorseDetailDto parent1, HorseDetailDto parent2) {
    LOG.trace("entityToDto({})", horse);
    if (horse == null) {
      return null;
    }

    return new HorseListDto(
        horse.id(),
        horse.name(),
        horse.description(),
        horse.dateOfBirth(),
        horse.sex(),
        getOwner(horse, owners),
        parent1,
        parent2

    );
  }

  /**
   * Converts a {@link Horse} entity into a {@link HorseDetailDto}.
   * The given maps must contain the owners and parents referenced by the horse.
   *
   * @param horse   the horse entity to convert
   * @param owners  a map of horse owners by their ID
   * @return the converted {@link HorseDetailDto}
   */
  public HorseDetailDto entityToDetailDto(
      Horse horse,
      Map<Long, OwnerDto> owners,
      HorseDetailDto parent1,
      HorseDetailDto parent2) {
    LOG.trace("entityToDto({})", horse);
    if (horse == null) {
      return null;
    }
    return new HorseDetailDto(
        horse.id(),
        horse.name(),
        horse.description(),
        horse.dateOfBirth(),
        horse.sex(),
        horse.image(),
        getOwner(horse, owners),
        parent1,
        parent2
    );
  }

  public HorseCreateDto updateDTOToCreateDTO(HorseUpdateDto horse){

    return new HorseCreateDto(horse.name(),
            horse.description(),
            horse.dateOfBirth(),
            horse.sex(),
            horse.ownerId(),
            horse.parentId1(),
            horse.parentId2());

  }

  private OwnerDto getOwner(Horse horse, Map<Long, OwnerDto> owners) {
    LOG.trace("getOwner() with parameters: {}, {}", horse, owners);
    OwnerDto owner = null;
    var ownerId = horse.ownerId();
    if (ownerId != null) {
      if (!owners.containsKey(ownerId)) {
        throw new FatalException("Given owner map does not contain owner of this Horse (%d)".formatted(horse.id()));
      }
      owner = owners.get(ownerId);
    }
    return owner;
  }

}
