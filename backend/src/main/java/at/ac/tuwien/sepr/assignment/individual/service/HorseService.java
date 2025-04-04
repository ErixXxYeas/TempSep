package at.ac.tuwien.sepr.assignment.individual.service;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseTreeNodeDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;

import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

/**
 * Service for working with horses.
 */
public interface HorseService {

  /**
   * Lists all horses stored in the system, that fit the parameters.
   *
   * @param params the parameters which will bes used to search the horses
   * @return list of all stored horses
   */
  Stream<HorseListDto> horsesByParameters(HorseSearchDto params);

  /**
   * Updates the horse with the ID given in {@code horse}
   * with the data given in {@code horse}
   * in the persistent data store.
   *
   * @param horse the horse to update
   * @return he updated horse
   * @throws NotFoundException if the horse with given ID does not exist in the persistent data store
   * @throws ValidationException if the update data given for the horse is in itself incorrect (description too long, no name, …)
   * @throws ConflictException if the update data given for the horse is in conflict the data currently in the system (owner does not exist, …)
   */
  HorseDetailDto update(HorseUpdateDto horse, MultipartFile image) throws NotFoundException, ValidationException, ConflictException, IOException;

  /**
   * Get the horse with given ID, with more detail information.
   * This includes the owner of the horse, and its parents.
   * The parents of the parents are not included.
   *
   * @param id the ID of the horse to get
   * @return the horse with ID {@code id}
   * @throws NotFoundException if the horse with the given ID does not exist in the persistent data store
   */
  HorseDetailDto getById(long id) throws NotFoundException;

  InputStream getHorseImage(long id) throws NotFoundException;

  /**
   * Get the horse with given ID, with more detail information.
   * This includes the owner of the horse, and its parents.
   * The parents of the parents are not included.
   *
   * @param id the ID of the horse to get
   * @param generations the depth of horse generations
   * @return the horse with ID {@code id}
   * @throws NotFoundException if the horse with the given ID does not exist in the persistent data store
   */

  HorseTreeNodeDto getByIdForTree(long id, long generations) throws NotFoundException;

  /**
   * Creates a horse with the given Information
   * in {@code horse} with the data given in
   * {@code horse} in the persistent data store.
   *
   * @param horse the horse to create
   * @param image the image which belongs to the horse
   * @throws ValidationException if the update data given for the horse is in itself incorrect (description too long, no name, …)
   * @throws ConflictException if the update data given for the horse is in conflict the data currently in the system (owner does not exist, …)
   */
  Horse create(HorseCreateDto horse, MultipartFile image) throws ValidationException, ConflictException, NotFoundException, IOException;

  /**
   * Deletes the horse with given ID.
   *
   * @param id the ID of the horse to delete
   * @throws NotFoundException if the horse with the ID does not exist in the persistent data store
   */
  void deleteById(long id) throws NotFoundException;
  HorseDetailDto removeImageById(long id) throws NotFoundException;



}
