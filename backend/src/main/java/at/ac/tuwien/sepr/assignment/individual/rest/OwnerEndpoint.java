package at.ac.tuwien.sepr.assignment.individual.rest;

import at.ac.tuwien.sepr.assignment.individual.dto.*;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.service.OwnerService;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Base64;
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
   * @param searchParameters the parameters to filter the owner search
   * @return a stream of {@link OwnerDto} matching the search criteria
   */
  @GetMapping
  public Stream<OwnerDto> search(OwnerSearchDto searchParameters) throws NotFoundException {
    LOG.info("GET " + BASE_PATH + " query parameters: {}", searchParameters);
    if(searchParameters == null){
      return service.getAll();
    }
    return service.search(searchParameters);
  }

  @PostMapping
  public OwnerCreateDto create(
          @RequestBody OwnerCreateDto toCreate)throws IOException{
    LOG.info("Post " + BASE_PATH, toCreate);

      return service.create(toCreate);
  }

  @DeleteMapping("{id}")
  public void deleteById(@PathVariable("id") long id) {
    LOG.info("Delete " + BASE_PATH + "/{}", id);
    try {
      service.deleteById(id);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }

}
