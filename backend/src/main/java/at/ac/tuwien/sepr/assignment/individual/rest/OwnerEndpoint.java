package at.ac.tuwien.sepr.assignment.individual.rest;

import at.ac.tuwien.sepr.assignment.individual.dto.*;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.service.OwnerService;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST controller for managing owner-related operations.
 * Provides endpoints for searching and creating owners.
 */
@RestController
@RequestMapping(OwnerEndpoint.BASE_PATH)
public class OwnerEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  static final String BASE_PATH = "/owners";
  private final OwnerService service;

  public OwnerEndpoint(OwnerService service) {
    this.service = service;
  }

  /**
   * Searches for owners based on the given search parameters.
   *
   * @param name the name of the owner
   * @param maxAmount the Amount of owners fetched
   * @return a stream of {@link OwnerDto} matching the search criteria
   * @throws NotFoundException
   */
  @GetMapping
  public Stream<OwnerDto> search(@RequestParam(value = "name", required = false) String name,
                                 @RequestParam(value = "maxAmount", required = false) Integer maxAmount) throws NotFoundException {
    LOG.info("GET {} query parameters: {}, {}", BASE_PATH, name, maxAmount );
    try {
      if (name == null && maxAmount == null) {
        return service.getAll();
      }
      OwnerSearchDto searchParameters = new OwnerSearchDto(name, maxAmount);
      return service.search(searchParameters);
    } catch (NotFoundException e) {
      LOG.warn("GET {} - No owners found with parameters: {} , {}", BASE_PATH, name, maxAmount );
      throw new NotFoundException(e);
    }

  }

  /**
   * Creates a new Owner with the provided data
   *
   * @param toCreate contains the data for the new Owner
   * @throws IOException if the given data is Invalid
   */
  @PostMapping
  public void create(
          @RequestBody OwnerCreateDto toCreate) throws ValidationException {
    LOG.info("POST: {} " + BASE_PATH, toCreate);

    try {
      service.create(toCreate);
    } catch ( ValidationException e) {
      LOG.error("Error while creating owner: {}", toCreate, e);
      throw e;
    }

  }

  /**
   * Deletes an Owner by their ID
   *
   * @param id Unique number which determines what owner will be deleted
   */
  @DeleteMapping("{id}")
  public void deleteById(@PathVariable("id") long id) {
    LOG.info("DELETE: {} " + BASE_PATH, id);
    try {
      service.deleteById(id);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      LOG.warn("DELETE: {} " + BASE_PATH + " not found", id);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }

}
