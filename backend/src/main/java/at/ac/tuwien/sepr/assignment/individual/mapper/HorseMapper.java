package at.ac.tuwien.sepr.assignment.individual.mapper;


import at.ac.tuwien.sepr.assignment.individual.dto.*;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


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
  public HorseListDto entityToListDto(Horse horse, Map<Long, OwnerDto> owners) {
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
            horse.parentId1(),
            horse.parentId2()

    );
  }

  /**
   * Converts a {@link Horse} entity into a {@link HorseDetailDto}.
   * The given maps must contain the owners and parents referenced by the horse.
   *
   * @param horse  the horse entity to convert
   * @param owners a map of horse owners by their ID
   * @return the converted {@link HorseDetailDto}
   */
  public HorseDetailDto entityToDetailDto(
          Horse horse,
          Map<Long, OwnerDto> owners) {
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
            horse.parentId1(),
            horse.parentId2()
    );
  }

  /**
   * Converts a {@link Horse} entity into a {@link HorseTreeNodeDto}.
   * The given maps must contain the owners and parents referenced by the horse.
   *
   * @param horse  the horse entity to convert
   * @param owners a map of horse owners by their ID
   * @param parent1 a map of horse owners by their ID
   * @param parent2 a map of horse owners by their ID
   * @return the converted {@link HorseTreeNodeDto}
   */
  public HorseTreeNodeDto entityToTreeNodeDto(
          Horse horse,
          HorseTreeNodeDto parent1,
          HorseTreeNodeDto parent2
          ) {
    LOG.trace("entityToDto({})", horse);
    if (horse == null) {
      return null;
    }
    return new HorseTreeNodeDto(
            horse.id(),
            horse.name(),
            horse.dateOfBirth(),
            horse.sex(),
            parent1,
            parent2
    );
  }

  /**
   * Converts a {@link HorseUpdateDto} DTO into a {@link HorseDetailDto}.
   * The given maps must contain the owners and parents referenced by the horse.
   *
   * @param horse the horse we want to convert into a {@link HorseDetailDto}.
   * @return the converted {{@link HorseCreateDto}
   */
  public HorseCreateDto updateDTOToCreateDTO(HorseUpdateDto horse) {
    LOG.trace("updateDTOToCreateDTO({})", horse);
    return new HorseCreateDto(horse.name(),
            horse.description(),
            horse.dateOfBirth(),
            horse.sex(),
            horse.ownerId(),
            horse.parentId1(),
            horse.parentId2());
  }

  /**
   * Converts a {@link HorseDetailDto} DTO into a {@link HorseCreateDto}.
   * The given maps must contain the owners and parents referenced by the horse.
   *
   * @param horse the horse we want to convert into a {@link HorseCreateDto}.
   * @return the converted {@link HorseCreateDto}
   */
  public HorseCreateDto detailDTOToCreateDTO(HorseDetailDto horse) {
    LOG.trace("detailDTOToCreateDTO({})", horse);
    return new HorseCreateDto(horse.name(),
            horse.description(),
            horse.dateOfBirth(),
            horse.sex(),
            horse.owner() != null ? horse.owner().id() : null,
            horse.parent1Id(),
            horse.parent1Id());
  }

  /**
   * Fetches the owner of a horse from a map of owners
   *
   * @param horse  the horse which we're searching the owner of
   * @param owners a map of owners
   * @return The owner of the horse
   */

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
