package at.ac.tuwien.sepr.assignment.individual.rest;

import at.ac.tuwien.sepr.assignment.individual.dto.*;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.service.HorseService;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.stream.Stream;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


/**
 * REST controller for managing horse-related operations.
 * Provides endpoints for searching, retrieving, creating, updating, and deleting horses,
 * as well as fetching their family tree.
 */
@RestController
@RequestMapping(path = HorseEndpoint.BASE_PATH)
public class HorseEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  static final String BASE_PATH = "/horses";

  private final HorseService service;

  @Autowired
  public HorseEndpoint(HorseService service) {
    this.service = service;
  }

  /**
   * Searches for horses based on the given search parameters.
   *
   * @param searchParameters the parameters to filter the horse search
   * @return a stream of {@link HorseListDto} matching the search criteria
   */
  @GetMapping
  public Stream<HorseListDto> searchHorses(HorseSearchDto searchParameters) {
    LOG.info("GET " + BASE_PATH);
    LOG.debug("request parameters: {}", searchParameters);
    // TODO We have the request params in the DTO now, but don't do anything with them yet…

    return service.allHorses();
  }

  /**
   * Retrieves the details of a horse by its ID.
   *
   * @param id the unique identifier of the horse
   * @return the detailed information of the requested horse
   * @throws ResponseStatusException if the horse is not found
   */
  @GetMapping("{id}")
  public HorseDetailDto getById(@PathVariable("id") long id) {
    LOG.info("GET " + BASE_PATH + "/{}", id);
    try {
      return service.getById(id);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Horse to get details of not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }


  /**
   * Updates the details of an existing horse, including an optional image file.
   *
   * @param id        the ID of the horse to update
   * @param toUpdate  the updated horse data
   * @return the updated horse details
   * @throws ValidationException     if validation fails
   * @throws ConflictException       if a conflict occurs while updating
   * @throws ResponseStatusException if the horse is not found
   */
  @PutMapping(path = "{id}", consumes ={MediaType.MULTIPART_FORM_DATA_VALUE} )
  public HorseDetailDto update(
      @PathVariable("id") long id,
      @RequestParam("name") String name,
      @RequestParam("dateOfBirth")LocalDate dateOfBirth,
      @RequestParam("sex") Sex sex,
      @RequestParam(value = "description", required = false) String description,
      @RequestParam(value = "ownerId", required = false) Long ownerId,
      @RequestParam(value = "parentId1", required = false) Long parentId1,
      @RequestParam(value = "parentId2", required = false) Long parentId2,
      @RequestPart(value = "image", required = false) MultipartFile image)
          throws IOException {

    try {
      HorseUpdateRestDto toUpdate = new HorseUpdateRestDto(name,description, dateOfBirth , sex, ownerId, parentId1, parentId2);
      LOG.info("PUT " + BASE_PATH);
      LOG.debug("Body of request:\n{}", toUpdate);
      return service.update(toUpdate.toUpdateDtoWithId(id), image);


    } catch (NotFoundException | ValidationException | ConflictException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Horse to update not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }

  /**
   * Creates a new horse with the provided details.
   *
   * @param toCreate  the horse data to create
   * @return the created horse details
   * @throws ValidationException     if validation fails
   * @throws ConflictException       if a conflict occurs while creating
   */
  @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public HorseCreateDto create(
          @RequestPart("horse") String horse,
          @RequestPart(value = "image", required = false) MultipartFile image)
        throws IOException {



    HorseCreateDto toCreate = new HorseCreateDto(name,description,dateOfBirth,sex,ownerId,parentId1,parentId2);

    LOG.info("POST " + BASE_PATH);
    LOG.debug("Body of request:\n{}", toCreate);

    return service.create(toCreate, image);
  }

  @DeleteMapping("{id}")
  public void deleteById(@PathVariable("id") long id) {
    LOG.info("Delete " + BASE_PATH + "/{}", id);
    try {
      service.deleteById(id);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Horse to get details of not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }


  /**
   * Logs client-side errors with relevant details.
   *
   * @param status  the HTTP status code of the error
   * @param message a brief message describing the error
   * @param e       the exception that occurred
   */
  private void logClientError(HttpStatus status, String message, Exception e) {
    LOG.warn("{} {}: {}: {}", status.value(), message, e.getClass().getSimpleName(), e.getMessage());
  }

}
