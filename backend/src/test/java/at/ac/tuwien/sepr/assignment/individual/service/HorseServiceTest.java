package at.ac.tuwien.sepr.assignment.individual.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test for {@link HorseService}.
 */
@ActiveProfiles({"test", "datagen"}) // Enables "test" Spring profile during test execution
@SpringBootTest
public class HorseServiceTest {

  @Autowired
  HorseService horseService;

  /**
   * Tests whether retrieving all stored horses returns the expected number and specific entries.
   */
  @Test
  public void getAllReturnsAllStoredHorses() {
    HorseSearchDto searchParameters = new HorseSearchDto(null, null, null, null, null, null, null, null);
    List<HorseListDto> horses = horseService.horsesByParameters(searchParameters)
            .toList();
    assertThat(horses.size()).isGreaterThanOrEqualTo(1);

    assertThat(horses)
            .map(HorseListDto::id, HorseListDto::sex)
            .contains(tuple(-1L, Sex.FEMALE));
  }

  /**
   * Tests if a horse can be fetched by their id
   *
   * @throws NotFoundException if horse doesn't exist
   */
  @Test
  public void getByIdReturnsHorseIfExists() throws NotFoundException {
    long horseId = -1L;

    HorseDetailDto horse = horseService.getById(horseId);

    assertThat(horse).isNotNull();
    assertThat(horse.id()).isEqualTo(horseId);
    assertThat(horse.name()).isNotEmpty();
  }

  /**
   * Tests if you can fetch a horse which doesn't exist
   */
  @Test
  public void getByIdThrowsNotFoundExceptionIfHorseDoesNotExist() {
    assertThrows(NotFoundException.class, () -> {
      horseService.getById(-69L);
    });
  }

  /**
   * Tests if you can create a Horse successfully
   *
   * @throws ValidationException if there is a validation error
   * @throws ConflictException   if the horse already exists
   * @throws NotFoundException   if the horse does not exist
   */
  @Test
  public void createHorseSuccessfully() throws ValidationException, ConflictException, NotFoundException, IOException {
    HorseCreateDto horseDto = new HorseCreateDto(
            "Terry Test",
            "Just a test horse",
            LocalDate.of(2000, 2, 20),
            Sex.MALE,
            null,
            null,
            null
    );

    Horse createdHorse = horseService.create(horseDto, null);

    HorseDetailDto createdHorseDetails = horseService.getById(createdHorse.id());
    assertThat(createdHorseDetails).isNotNull();
    assertThat(createdHorseDetails.name()).isEqualTo("Terry Test");
  }

  /**
   * tests if deleting a horse actually deletes the horse
   *
   * @throws NotFoundException if the horse cannot be found
   */
  @Test
  public void deleteHorseSuccessfully() throws NotFoundException {
    long horseId = -1L;

    horseService.deleteById(horseId);
    assertThrows(NotFoundException.class, () -> {
      horseService.getById(-1L);
    });
  }


}
