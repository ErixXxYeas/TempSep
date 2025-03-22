package at.ac.tuwien.sepr.assignment.individual.persistence.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/**
 * JDBC implementation of {@link HorseDao} for interacting with the database.
 */
@Repository
public class HorseJdbcDao implements HorseDao {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String TABLE_NAME = "horse";

  private static final String SQL_SELECT_ALL =
          "SELECT * FROM " + TABLE_NAME;
  private static final String SQL_SELECT_ALL_BY_PARAMS =
          "SELECT * FROM " + TABLE_NAME
                  + " WHERE (:name IS NULL OR UPPER(name) LIKE UPPER('%%' || COALESCE(:name, '') || '%%')) "
                  + "AND (:description IS NULL OR description LIKE '%' || :description || '%') "
                  + "AND (:born_before IS NULL OR date_of_birth < :born_before) "
                  + "AND (:sex IS NULL OR sex = :sex) "
                  + "LIMIT :limit";

  private static final String SQL_SELECT_BY_ID =
          "SELECT * FROM " + TABLE_NAME
                  + " WHERE ID = :id";


  private static final String SQL_DELETE_BY_ID =
          "DELETE FROM " + TABLE_NAME
                  + " WHERE ID = :id";

  private static final String SQL_UPDATE =
          "UPDATE " + TABLE_NAME
                  + """
                      SET name = :name,
                          description = :description,
                          date_of_birth = :date_of_birth,
                          sex = :sex,
                          image = :image,
                          owner_id = :owner_id,
                          parent1_id = :parent1_id,
                          parent2_id = :parent2_id
                      WHERE id = :id
                  """;

  private static final String SQL_INSERT =
          "INSERT INTO "
                  + TABLE_NAME
                  + " (name, description, date_of_birth, sex, image, owner_id, parent1_id, parent2_id) "
                  + "VALUES (:name, :description, :date_of_birth, :sex, :image, :ownerId, :parent1_id, :parent2_id)";

  private final JdbcClient jdbcClient;

  @Autowired
  public HorseJdbcDao(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  @Override
  public List<Horse> getAll() {
    LOG.trace("getAll()");
    LOG.debug("SQL: {}", SQL_SELECT_ALL);
    return jdbcClient
            .sql(SQL_SELECT_ALL)
            .query(this::mapRow)
            .list();
  }

  @Override
  public List<Horse> getByParams(HorseSearchDto params) {
    LOG.trace("getByParams()");
    LOG.debug("SQL: {}", SQL_SELECT_ALL_BY_PARAMS);
    LOG.info("Sex param: '{}'", params.bornBefore());
    return jdbcClient
            .sql(SQL_SELECT_ALL_BY_PARAMS).param("name", params.name())
            .param("sex", params.sex() == null ? null : params.sex().toString())
            .param("born_before", params.bornBefore())
            .param("description", params.description())
            .param("limit", params.limit() == null ? Integer.MAX_VALUE : params.limit())
            .query(this::mapRow)
            .list();
  }

  @Override
  public Horse getById(long id) throws NotFoundException {
    LOG.trace("getById() with parameters: {}", id);
    LOG.debug("SQL: {}", SQL_SELECT_BY_ID);
    List<Horse> horses = jdbcClient
            .sql(SQL_SELECT_BY_ID)
            .param("id", id)
            .query(this::mapRow)
            .list();

    if (horses.isEmpty()) {
      throw new NotFoundException("No horse with ID %d found".formatted(id));
    }

    if (horses.size() > 1) {
      // This should never happen!!
      throw new FatalException("Too many horses with ID %d found".formatted(id));
    }

    return horses.getFirst();
  }

  @Override
  public Horse create(HorseCreateDto horse, byte[] image) throws IOException {
    LOG.trace("create() with parameters: {}", horse);
    LOG.debug("SQL: {} with parameters: {}", SQL_INSERT, horse);
    KeyHolder keyHolder = new GeneratedKeyHolder();
    int created = jdbcClient.sql(SQL_INSERT).param("name", horse.name())
            .param("description", horse.description())
            .param("date_of_birth", horse.dateOfBirth())
            .param("sex", horse.sex().toString())
            .param("image", image)
            .param("ownerId", horse.ownerId())
            .param("parent1_id", horse.parentId1())
            .param("parent2_id", horse.parentId2())
            .update(keyHolder);
    if (created == 0) {
      LOG.error("Error: Horse data is null.");
      throw new IOException("Could not create horse: " + horse);
    }
    LOG.info("Successfully inserted horse with name: {}", horse.name());

    return new Horse(keyHolder.getKey().longValue(),
            horse.name(),
            horse.description(),
            horse.dateOfBirth(),
            horse.sex(),
            image,
            horse.ownerId(),
            horse.parentId1(),
            horse.parentId2());

  }


  @Override
  public void delete(Long id) throws NotFoundException {
    LOG.trace("delete()  with parameters: {}", id);
    LOG.debug("SQL: {} with id: {}", SQL_DELETE_BY_ID, id);
    jdbcClient.sql(SQL_DELETE_BY_ID)
            .param("id", id).update();
  }


  @Override
  public Horse update(HorseUpdateDto horse, byte[] image) throws NotFoundException {
    LOG.trace("update() with parameters: {} , {}", horse, image);
    LOG.debug("SQL: {} with parameters: {}", SQL_INSERT, horse);
    int updated = jdbcClient
            .sql(SQL_UPDATE)
            .param("id", horse.id())
            .param("name", horse.name())
            .param("description", horse.description())
            .param("date_of_birth", horse.dateOfBirth())
            .param("sex", horse.sex().toString())
            .param("image", image)
            .param("owner_id", horse.ownerId())
            .param("parent1_id", horse.parentId1())
            .param("parent2_id", horse.parentId2())
            .update();

    if (updated == 0) {
      throw new NotFoundException(
              "Could not update horse with ID " + horse.id() + ", because it does not exist"
      );
    }
    LOG.info("Successfully updated horse with name: {}", horse.name());
    return new Horse(
            horse.id(),
            horse.name(),
            horse.description(),
            horse.dateOfBirth(),
            horse.sex(),
            image,
            horse.ownerId(),
            horse.parentId1(),
            horse.parentId2());
  }


  private Horse mapRow(ResultSet result, int rownum) throws SQLException {
    LOG.trace("maptRow() with parameters: {} , {}", result, rownum);
    return new Horse(
            result.getLong("id"),
            result.getString("name"),
            result.getString("description"),
            result.getDate("date_of_birth").toLocalDate(),
            Sex.valueOf(result.getString("sex")),
            result.getBytes("image"),
            result.getObject("owner_id", Long.class),
            result.getObject("parent1_id", Long.class),
            result.getObject("parent2_id", Long.class));
  }
}
