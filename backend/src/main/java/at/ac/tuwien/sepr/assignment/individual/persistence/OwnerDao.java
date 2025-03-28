package at.ac.tuwien.sepr.assignment.individual.persistence;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerSearchDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Owner;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;

import java.io.IOException;
import java.util.Collection;

/**
 * Data Access Object for owners.
 * Implements CRUD functionality for managing owners in the persistent data store.
 */
public interface OwnerDao {


  /**
   * Creates an owner with the given data
   *
   * @param owner contains the data for the created owner
   * @throws IOException if there is no Data
   */
  void create(OwnerCreateDto owner) throws IOException;

  /**
   * Fetch an owner from the persistent data store by its ID.
   *
   * @param id the ID of the owner to get
   * @return the owner with the ID {@code id}
   * @throws NotFoundException if no owner with the given ID exists in the persistent data store
   */
  Owner getById(long id) throws NotFoundException;

  /**
   * Fetch a set of owners by their IDs from the persistent data store.
   * This is best effort, if some owner can not be found in the data store, it is simply not in the returned set.
   *
   * @param ids a collection of ids, to fetch the referenced owners by.
   * @return the collection of all found owners, without those, that are not in the persistent data store
   * @throws NotFoundException if the Owner with the given IDs do not exist in the persistent data store
   */
  Collection<Owner> getAllById(Collection<Long> ids) throws NotFoundException;

  /**
   * Search for owners matching the criteria in {@code searchParameters}.
   *
   * <p>
   * A owner is considered matched, if its name contains {@code searchParameters.name} as a substring.
   * The returned stream of owners never contains more than {@code searchParameters.maxAmount} elements,
   * even if there would be more matches in the persistent data store.
   * </p>
   *
   * @param searchParameters object containing the search parameters to match
   * @return a stream containing owners matching the criteria in {@code searchParameters}
   * @throws NotFoundException if the Owner with the given does not exist in the persistent data store
   */
  Collection<Owner> search(OwnerSearchDto searchParameters) throws NotFoundException;

  /**
   * Get all Owners stored in the persistent data store.
   *
   * @return a list of all stored horses
   */
  Collection<Owner> getAll();

  /**
   * Deletes the horse with the ID given in {@code horse}
   * in the persistent data store.
   *
   * @param id the ID of the horse to get
   * @throws NotFoundException if the Owner with the given ID does not exist in the persistent data store
   */
  void delete(Long id) throws NotFoundException;

}
