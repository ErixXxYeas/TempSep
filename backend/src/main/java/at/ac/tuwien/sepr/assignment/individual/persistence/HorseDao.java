package at.ac.tuwien.sepr.assignment.individual.persistence;


import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Data Access Object for horses.
 * Implements access functionality to the application's persistent data store regarding horses.
 */
public interface HorseDao {

  /**
   * Get all horses stored in the persistent data store.
   *
   * @return a list of all stored horses
   */
  List<Horse> getByParams(HorseSearchDto params);

  /**
   * Update the horse with the ID given in {@code horse}
   * with the data given in {@code horse}
   * in the persistent data store.
   *
   * @param horse the horse to update
   * @return the updated horse
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  Horse update(HorseUpdateDto horse, InputStream image) throws NotFoundException;


  /**
   * Get a horse by its ID from the persistent data store.
   *
   * @param id the ID of the horse to get
   * @return the horse
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  Horse getById(long id) throws NotFoundException;

  /**
   * Creates a horse with the data given in
   * {@code horse} in the persistent data store.
   *
   * @param horse the horse that will get created
   * @throws IOException if there is no Data
   */
  Horse create(HorseCreateDto horse, InputStream image) throws IOException;

  /**
   * Deletes the horse with the ID given in {@code horse}
   * in the persistent data store.
   *
   * @param id the ID of the horse to get
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  void delete(Long id) throws NotFoundException;


  /**
   * Deletes the image of a horse with the ID given in {@code horse}
   * in the persistent data store.
   *
   * @param id the ID of the horse to get
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  Horse removeImageById(Long id) throws NotFoundException;


}
