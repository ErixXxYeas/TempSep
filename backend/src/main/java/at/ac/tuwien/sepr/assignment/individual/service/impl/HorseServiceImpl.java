package at.ac.tuwien.sepr.assignment.individual.service.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerDto;
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
  public Stream<HorseListDto> allHorses() {
    LOG.trace("allHorses()");
    LOG.debug("Fetching all horses from the database");
    var horses = dao.getAll();
    var ownerIds = horses.stream()
            .map(Horse::ownerId)
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableSet());
    Map<Long, OwnerDto> ownerMap;
    try {
      ownerMap = ownerService.getAllById(ownerIds);
    } catch (NotFoundException e) {
      throw new FatalException("Horse, that is already persisted, refers to non-existing owner", e);
    }
    return horses.stream()
            .map(horse -> {
              HorseDetailDto parent1 = fetchParent(horse.parentId1());
              HorseDetailDto parent2 = fetchParent(horse.parentId2());
              return mapper.entityToListDto(horse, ownerMap, parent1, parent2);
            });

  }

  private HorseDetailDto fetchParent(Long parentId) {
    LOG.trace("fetchParent() with parameters: {}", parentId);
    if (parentId != null) {
      try {
        return getById(parentId);
      } catch (NotFoundException e) {
        throw new RuntimeException("Parent with ID " + parentId + " not found", e);
      }
    }
    return null;
  }


  @Override
  public HorseDetailDto update(HorseUpdateDto horse, MultipartFile image) throws NotFoundException, ValidationException, ConflictException, IOException {
    LOG.trace("update() with parameters: {}", horse);
    validator.validateForUpdate(horse);


    byte[] imageBytes = null;
    if (image != null) {
      imageBytes = image.getBytes();
    }

    HorseDetailDto parent1 = null;
    HorseDetailDto parent2 = null;

    if (horse.parentId1() != null) {
      parent1 = getById(horse.parentId1());
    }

    if (horse.parentId2() != null) {
      parent2 = getById(horse.parentId2());
    }

    var updatedHorse = dao.update(horse, imageBytes);
    return mapper.entityToDetailDto(
            updatedHorse,
            ownerMapForSingleId(updatedHorse.ownerId()), parent1, parent2);
  }


  @Override
  public HorseDetailDto getById(long id) throws NotFoundException {
    LOG.trace("getById() with parameters: {}", id);
    try {
      Horse horse = dao.getById(id);
      HorseDetailDto parent1 = null;
      HorseDetailDto parent2 = null;
      if (horse.parentId1() != null) {
        parent1 = getById(horse.parentId1());
      }
      if (horse.parentId2() != null) {
        parent2 = getById(horse.parentId2());
      }
      return mapper.entityToDetailDto(
              horse,
              ownerMapForSingleId(horse.ownerId()), parent1, parent2);
    } catch (NotFoundException e) {
      LOG.warn("Horse with ID {} not found, throwing exception", id);
      throw new NotFoundException("Horse couldn't be found");
    }
  }


  @Override
  public void create(HorseCreateDto horse, MultipartFile image) throws ValidationException, ConflictException, NotFoundException {
    LOG.trace("create() with parameters: {} , {}", horse, image);
    validator.validateForCreate(horse);


    try {
      byte[] imageBytes = null;
      if (image != null) {
        imageBytes = image.getBytes();
      }
      dao.create(horse, imageBytes);
    } catch (Exception e){
      LOG.error("Error while creating horse: {}", horse, e);
    }

  }

  @Override
  public void deleteById(long id) throws NotFoundException {
    LOG.trace("delete() with parameters: {}", id);

    try {
      dao.delete(id);
    } catch (NotFoundException e){
      LOG.warn("deleteById(): - Horse not found");
      throw new NotFoundException(e);
    }


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
