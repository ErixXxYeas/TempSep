package at.ac.tuwien.sepr.assignment.individual.persistence.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerSearchDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Owner;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.persistence.OwnerDao;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * JDBC implementation of {@link OwnerDao} for interacting with the database.
 */
@Repository
public class OwnerJdbcDao implements OwnerDao {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String TABLE_NAME = "owner";
  private static final String SQL_SELECT_BY_ID =
      "SELECT * FROM " + TABLE_NAME
          + " WHERE id = :id";

  private static final String SQL_SELECT_ALL =
      "SELECT * FROM " + TABLE_NAME
          + " WHERE id IN (:ids)";

  private static final String SQL_DELETE_BY_ID =
          "DELETE FROM " + TABLE_NAME
                  + " WHERE ID = :id";

  private static final String SQL_GET_ALL =
          "SELECT * FROM " + TABLE_NAME;

  private static final String SQL_INSERT =
          "INSERT INTO "
                  + TABLE_NAME
                  + " (first_name, last_name, description) "
                  + "VALUES (:first_name, :last_name, :description)";

  private static final String SQL_SELECT_SEARCH =
      "SELECT * FROM " + TABLE_NAME
          + " WHERE UPPER(first_name || ' ' || last_name) LIKE UPPER('%%' || COALESCE(:name, '') || '%%')";

  private static final String SQL_SELECT_SEARCH_LIMIT_CLAUSE = " LIMIT :limit";

  private final JdbcClient jdbcClient;

  @Autowired
  public OwnerJdbcDao(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  @Override
  public void create(OwnerCreateDto owner) throws IOException {
    LOG.trace("create() with parameters: {}", owner);
    LOG.debug("SQL: {} with parameters: {}", SQL_INSERT, owner);
    if(owner != null){
      jdbcClient.sql(SQL_INSERT).param("first_name", owner.firstName())
              .param("last_name", owner.lastName())
              .param("description", owner.description())
              .update();
    } else {
      LOG.error("Error: Owner is null");
      throw new IOException("Owner is null");
    }
    LOG.info("Successfully created Owner with name: {}", owner.firstName());
  }

  @Override
  public Owner getById(long id) throws NotFoundException {
    LOG.trace("getById() with parameters: {}", id);
    LOG.debug("SQL: {} with parameters: {}", SQL_SELECT_BY_ID, id);
    List<Owner> owners = jdbcClient
        .sql(SQL_SELECT_BY_ID)
        .param("id", id)
        .query(this::mapRow)
        .list();
    if (owners.isEmpty()) {
      throw new NotFoundException("Owner with ID %d not found".formatted(id));
    }
    if (owners.size() > 1) {
      // If this happens, something is wrong with either the DB or the select
      throw new FatalException("Found more than one owner with ID %d".formatted(id));
    }
    LOG.info("Successfully fetched Owner with id: {}", id);
    return owners.getFirst();
  }


  @Override
  public Collection<Owner> getAllById(Collection<Long> ids) {
    LOG.trace("getAllById() with parameters: {}", ids);
    LOG.debug("SQL: {} with parameters: {}", SQL_SELECT_ALL, ids);
    LOG.info("Successfully fetched Owners by ids");
    return jdbcClient
        .sql(SQL_SELECT_ALL)
        .param("ids", ids)
        .query(this::mapRow)
        .list();



  }

  @Override
  public Collection<Owner> search(OwnerSearchDto searchParameters) throws NotFoundException {
    LOG.trace("search() with parameters: {}", searchParameters);
    LOG.debug("SQL: {} with parameters: {}", SQL_SELECT_SEARCH, searchParameters);
    var query = SQL_SELECT_SEARCH;

    Map<String, Object> params = new HashMap<>();
    params.put("name", searchParameters.name());

    var maxAmount = searchParameters.maxAmount();
    if (maxAmount != null) {
      query += SQL_SELECT_SEARCH_LIMIT_CLAUSE;
      params.put("limit", maxAmount);
    }

    Collection<Owner> owners = jdbcClient
        .sql(query)
        .params(params)
        .query(this::mapRow)
        .list();

    if (owners.isEmpty()){
      throw new NotFoundException("No owner with ID %d found");
    }
    LOG.info("Successfully fetched Owners");
    return owners;
  }
  @Override
  public Collection<Owner> getAll() {
    LOG.trace("getAll()");
    LOG.debug("SQL: {} ", SQL_SELECT_ALL);
    return jdbcClient
            .sql(SQL_GET_ALL)
            .query(this::mapRow)
            .list();
  }

  @Override
  public void delete(Long id) throws NotFoundException {
    LOG.trace("delete()  with parameters: {}", id);
    LOG.debug("SQL: {} with parameters: {}", SQL_DELETE_BY_ID, id);
    jdbcClient.sql(SQL_DELETE_BY_ID)
            .param("id", id).update();

  }

  private Owner mapRow(ResultSet resultSet, int i) throws SQLException {
    LOG.trace("mapRow()  with parameters: {} , {}", resultSet, i);
    return new Owner(
        resultSet.getLong("id"),
        resultSet.getString("first_name"),
        resultSet.getString("last_name"),
        resultSet.getString("description"));
  }
}
