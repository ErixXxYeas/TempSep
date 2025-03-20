package at.ac.tuwien.sepr.assignment.individual.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test for {@link HorseDao}, ensuring database operations function correctly.
 */
@ActiveProfiles({"test", "datagen"}) // Enables "test" Spring profile to load test data
@SpringBootTest
public class HorseDaoTest {

  @Autowired
  HorseDao horseDao;


  /**
   * Tests that creating a horse and verifies that the
   * specific horse exists in the test dataset.
   */
  @Test
  public void createHorse() throws IOException {
    HorseCreateDto horse = new HorseCreateDto("Tesie Test", "Just a test horse", LocalDate.of(2000,02,20), Sex.FEMALE,null,null,null );
    Horse createdHorse = horseDao.create(horse, null);
    assertThat(createdHorse.id()).isNotNull();
    assertThat(createdHorse.name()).isEqualTo("Tesie Test");
  }

  /**
   * Tests that creating a horse and updating it then verifies that the
   * specific horse exists in the test dataset.
   */
  @Test
  public void updateHorse() throws NotFoundException {


    HorseUpdateDto updateDto = new HorseUpdateDto(-1L, "Terry Test", "Same test horse", LocalDate.of(2000,02,20), Sex.FEMALE, null, null,null);
    Horse horseUpdated = horseDao.update(updateDto,null);
    assertThat(horseUpdated.id()).isEqualTo(-1L);
    assertThat(horseUpdated.name()).isEqualTo("Terry Test");
    assertThat(horseUpdated.description()).isEqualTo("Same test horse");
  }

  /**
   * Tests that creating a horse and deleting it works
   */
  @Test
  public void createHorseWithNoData() throws IOException, NotFoundException {
    HorseCreateDto horse = new HorseCreateDto("Tesie Test", "Just a test horse", LocalDate.of(2000,02,20), Sex.FEMALE,null,null,null );
    Horse createdHorse = horseDao.create(horse, null);

    horseDao.delete(createdHorse.id());
    assertThrows(NotFoundException.class, () -> {
      horseDao.getById(createdHorse.id());
    });
  }

  /**
   * Tests that fetching a horse, which does not exist returns an exception
   */
  @Test
  public void getHorseByIdNotFound(){
    assertThrows(NotFoundException.class, () -> {
      horseDao.getById(-69L);
    });
  }

  /**
   * Tests that retrieving all stored horses returns at least one entry
   * and verifies that a specific horse exists in the test dataset.
   */
  @Test
  public void getAllReturnsAllStoredHorses() {
    List<Horse> horses = horseDao.getAll();
    System.out.println(horses);
    assertThat(horses.size()).isGreaterThanOrEqualTo(1);
    assertThat(horses)
        .extracting(Horse::id, Horse::name)
        .contains(tuple(-1L, "Wendy"));
  }
}
