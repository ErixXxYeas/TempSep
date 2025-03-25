package at.ac.tuwien.sepr.assignment.individual.service.impl;


import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseTreeNodeDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;


import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.mapper.HorseMapper;
import at.ac.tuwien.sepr.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepr.assignment.individual.service.HorseService;
import at.ac.tuwien.sepr.assignment.individual.service.OwnerService;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementation of {@link HorseService} for handling image storage and retrieval.
 */
@Service
public class HorseServiceImpl implements HorseService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final HorseDao dao;
  private final HorseMapper mapper;
  private final HorseValidator validator;
  private final OwnerService ownerService;


  /**
   * Constructor of the HorseServiceImpl.
   *
   * @param dao          Persistence Layer of the horse
   * @param mapper       mapper used to map entities to horses and vice versa
   * @param validator    validator used to validate horses
   * @param ownerService used to access services for owners
   */
  @Autowired
  public HorseServiceImpl(HorseDao dao,
                          HorseMapper mapper,
                          HorseValidator validator,
                          OwnerService ownerService) {
    this.dao = dao;
    this.mapper = mapper;
    this.validator = validator;
    this.ownerService = ownerService;
  }

  @Override
  public Stream<HorseListDto> horsesByParameters(HorseSearchDto params) throws FatalException {
    LOG.trace("horsesByParameters() with the parameters: {}", params);
    LOG.debug("Fetching all horses from the database with search parameters");
    var horses = dao.getByParams(params);
    var ownerIds = horses.stream()
            .map(Horse::ownerId)
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableSet());
    Map<Long, OwnerDto> ownerMap;
    try {
      ownerMap = ownerService.getAllById(ownerIds);
    } catch (NotFoundException | FatalException e) {
      throw new FatalException("Horse, that is already persisted, refers to non-existing owner", e);
    }
    return horses.stream()
            .map(horse -> mapper.entityToListDto(horse, ownerMap));
  }

  @Override
  public HorseDetailDto update(HorseUpdateDto horse, MultipartFile image) throws NotFoundException, ValidationException, ConflictException, IOException {
    LOG.trace("update() with parameters: {}", horse);
    try (InputStream imageStream = (image != null) ? image.getInputStream() : null) {
      validator.validateForUpdate(horse);
      if (horse.parentId1() != null) {
        HorseDetailDto parent1 = getById(horse.parentId1());
        validator.validateHorseParents(parent1);

      }
      if (horse.parentId2() != null) {
        HorseDetailDto parent2 = getById(horse.parentId2());
        validator.validateHorseParents(parent2);
      }
      var updatedHorse = dao.update(horse, imageStream);
      return mapper.entityToDetailDto(
              updatedHorse,
              ownerMapForSingleId(updatedHorse.ownerId()));
    } catch (IOException e) {
      LOG.error("Error while creating horse: {}", horse, e);
      throw new IOException(e.getMessage(), null);
    }
  }


  @Override
  public HorseDetailDto getById(long id) throws NotFoundException {
    LOG.trace("getById() with parameters: {}", id);
    try {
      Horse horse = dao.getById(id);
      return mapper.entityToDetailDto(
              horse,
              ownerMapForSingleId(horse.ownerId()));
    } catch (NotFoundException e) {
      LOG.warn("Horse with ID {} not found, throwing exception", id);
      throw new NotFoundException("Horse couldn't be found");
    }
  }

  /**
   * Fetches the horse image from the database by the ID.
   *
   * @param horseId Unique identifier for the horse.
   * @return an Inputstream conatining the horse Image
   * @throws NotFoundException if the horse doesn't exist
   */
  public InputStream getHorseImage(long horseId) throws NotFoundException {
    Horse horse = dao.getById(horseId);
    if (horse.image() == null) {
      throw new NotFoundException("No image found for horse ID: " + horseId);
    }

    return horse.image();
  }


  @Override
  public HorseTreeNodeDto getByIdForTree(long id, long generations) throws NotFoundException {
    LOG.trace("getByIdForTree() with parameters: {} , {}", id, generations);
    try {
      HorseTreeNodeDto parent1 = null;
      HorseTreeNodeDto parent2 = null;
      Horse horse = dao.getById(id);
      if (generations > 1) {
        if (horse.parentId1() != null) {
          parent1 = getByIdForTree(horse.parentId1(), generations - 1);
        }
        if (horse.parentId2() != null) {
          parent2 = getByIdForTree(horse.parentId2(), generations - 1);
        }
      }
      return mapper.entityToTreeNodeDto(
              horse, parent1, parent2);
    } catch (NotFoundException e) {
      LOG.warn("Horse with ID {} not found, throwing exception", id);
      throw new NotFoundException("Horse couldn't be found");
    }
  }


  @Override
  public Horse create(HorseCreateDto horse, MultipartFile image) throws ValidationException, NotFoundException, IOException {
    LOG.trace("create() with parameters: {} , {}", horse, image);
    try (InputStream imageStream = (image != null) ? image.getInputStream() : null) {
      validator.validateForCreate(horse);

      if (horse.parentId1() != null) {
        validator.validateHorseParents(getById(horse.parentId1()));
      }
      if (horse.parentId2() != null) {
        validator.validateHorseParents(getById(horse.parentId2()));
      }
      return dao.create(horse, imageStream);
    } catch (IOException e) {
      LOG.error("Error while creating horse: {}", horse, e);
      throw new IOException(e.getMessage(), null);
    }

  }

  @Override
  public void deleteById(long id) throws NotFoundException {
    LOG.trace("deleteById() with parameters: {}", id);

    try {
      dao.delete(id);
    } catch (NotFoundException e) {
      LOG.warn("deleteById(): - Horse not found");
      throw new NotFoundException(e);
    }


  }

  @Override
  public HorseDetailDto removeImageById(long id) throws NotFoundException {
    LOG.trace("removeImageById() with parameters: {}", id);
    try {
      var updatedHorse = dao.removeImageById(id);
      return mapper.entityToDetailDto(
              updatedHorse,
              ownerMapForSingleId(updatedHorse.ownerId()));
    } catch (NotFoundException e) {
      LOG.error("Error while updating horse with id: {}", id, e);

    }
    return null;
  }


  private Map<Long, OwnerDto> ownerMapForSingleId(Long ownerId) {
    LOG.trace("ownerMapForSingleId() with parameters: {}", ownerId);
    try {
      return ownerId == null
              ? null
              : Collections.singletonMap(ownerId, ownerService.getById(ownerId));
    } catch (NotFoundException e) {
      throw new FatalException("Owner %d referenced by horse not found".formatted(ownerId));
    }
  }

}
