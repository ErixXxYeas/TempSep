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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    LOG.info("GET " + BASE_PATH + "/{}", searchParameters);
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
      LOG.warn("Error getting horse with ID {}: {}", id, e.getMessage(), e);
      logClientError(status, "Horse to get details of not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }


  /**
   * Updates the details of an existing horse, including an optional image file.
   *
   * @param id the ID of the horse to update
   * @param horse the updated horse data
   * @param image the updated horse image
   * @return the updated horse details
   * @throws ConflictException if the Owner or Parent does not exist
   * @throws ValidationException if the horse has invalid Data
   */
  @PutMapping(path = "{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public HorseDetailDto update(
          @PathVariable("id") long id,
          @RequestPart("horse") String horse,
          @RequestPart(value = "image", required = false) MultipartFile image)
          throws ConflictException, ValidationException {
    LOG.info("PUT " + BASE_PATH + "/{}, {}, {}", id, horse, image);
    LOG.debug("Received JSON horse: {}", horse);
    try {
      JsonObject horseJson = (JsonObject) JsonParser.parseString(horse);
      System.out.println(horseJson.has("description"));
      HorseUpdateRestDto toUpdate = new HorseUpdateRestDto(horseJson.get("name").getAsString(),
              horseJson.has("description") ? horseJson.get("description").getAsString() : null,
              LocalDate.parse(horseJson.get("dateOfBirth").getAsString()),
              Sex.valueOf(horseJson.get("sex").getAsString().toUpperCase()),
              horseJson.has("ownerId") ? horseJson.get("ownerId").getAsLong() : null,
              horseJson.has("parentId1") ? horseJson.get("parentId1").getAsLong() : null,
              horseJson.has("parentId2") ? horseJson.get("parentId2").getAsLong() : null);
      LOG.info("PUT " + BASE_PATH);
      LOG.debug("Body of request:\n{}", toUpdate);
      return service.update(toUpdate.toUpdateDtoWithId(id), image);

    } catch (NotFoundException | ValidationException | ConflictException | IOException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      LOG.warn("Error updating horse with ID {}: {}", id, e.getMessage(), e);
      logClientError(status, "Horse to update not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }

  /**
   * Creates a new horse with the provided details.
   *
   * @param horse the updated horse data
   * @param image the updated horse image
   * @throws ConflictException if the Owner or Parent does not exist
   * @throws ValidationException if the horse has invalid Data
   */
  @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<Void> create(
          @RequestPart("horse") String horse,
          @RequestPart(value = "image", required = false) MultipartFile image)
          throws ConflictException, ValidationException {
    LOG.info("POST " + BASE_PATH + "/{}, {}", horse, image);
    LOG.debug("Received JSON horse: {}", horse);
    try {

      JsonObject horseJson = (JsonObject) JsonParser.parseString(horse);
      HorseCreateDto toCreate = new HorseCreateDto(horseJson.get("name").getAsString(),
              horseJson.has("description") ? horseJson.get("description").getAsString() : null,
              LocalDate.parse(horseJson.get("dateOfBirth").getAsString()),
              Sex.valueOf(horseJson.get("sex").getAsString().toUpperCase()),
              horseJson.has("ownerId") ? horseJson.get("ownerId").getAsLong() : null,
              horseJson.has("parentId1") ? horseJson.get("parentId1").getAsLong() : null,
              horseJson.has("parentId2") ? horseJson.get("parentId2").getAsLong() : null);

      LOG.info("POST " + BASE_PATH);
      LOG.debug("Body of request:\n{}", toCreate);
      service.create(toCreate, image);
      return ResponseEntity.ok().build();
    } catch (ConflictException | ValidationException | NotFoundException e) {

      HttpStatus status = HttpStatus.CONFLICT;
      LOG.warn(e.getMessage(), e);
      logClientError(status, "Horse couldn't be created", e);
      throw new ResponseStatusException(status, e.getMessage(), e);

    }

  }

  /**
   * Deletes horse by their ID
   *
   * @param id Unique number to delete by
   */
  @DeleteMapping("{id}")
  public ResponseEntity<Void> deleteById(@PathVariable("id") long id) {
    LOG.info("Delete " + BASE_PATH + "/{}", id);
    try {
      service.deleteById(id);
      return ResponseEntity.noContent().build();
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      LOG.warn("Error deleting horse with ID {}: {}", id, e.getMessage(), e);
      logClientError(status, "Horse to delete of not found", e);
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
