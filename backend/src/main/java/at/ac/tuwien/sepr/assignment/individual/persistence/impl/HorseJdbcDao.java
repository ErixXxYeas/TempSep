package at.ac.tuwien.sepr.assignment.individual.persistence.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

/**
 * JDBC implementation of {@link HorseDao} for interacting with the database.
 */
@Repository
public class HorseJdbcDao implements HorseDao {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String TABLE_NAME = "horse";

  private static final String SQL_SELECT_ALL =
      "SELECT * FROM " + TABLE_NAME;

  private static final String SQL_SELECT_BY_ID =
      "SELECT * FROM " + TABLE_NAME
          + " WHERE ID = :id";

  private static final String SQL_UPDATE =
      "UPDATE " + TABLE_NAME
          + """
              SET name = :name,
                  description = :description,
                  date_of_birth = :date_of_birth,
                  sex = :sex
              WHERE id = :id
          """;

  private static final String SQL_INSERT =
          "INSERT INTO "
                  + TABLE_NAME
                  + " (name, description, date_of_birth, sex, image, owner_id) "
                  + "VALUES (:name, :description, :dateOfBirth, :sex, :image, :ownerId)";

  private final JdbcClient jdbcClient;

  @Autowired
  public HorseJdbcDao(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  @Override
  public List<Horse> getAll() {
    LOG.trace("getAll()");
    return jdbcClient
        .sql(SQL_SELECT_ALL)
        .query(this::mapRow)
        .list();
  }

  @Override
  public Horse getById(long id) throws NotFoundException {
    LOG.trace("getById({})", id);
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
  public void create(HorseCreateDto horse, byte[] image) throws IOException {
    LOG.trace("create()");

    if (horse != null) {
      jdbcClient.sql(SQL_INSERT).param("name", horse.name())
              .param("description", horse.description())
              .param("dateOfBirth", horse.dateOfBirth())
              .param("sex", horse.sex().toString())
              .param("image", image)
              .param("ownerId", horse.ownerId())
              .update();
    } else {
      LOG.error("Error: Horse data is null.");
    }
  }



  @Override
  public Horse update(HorseUpdateDto horse, byte[] image) throws NotFoundException {
    LOG.trace("update({})", horse);
    int updated = jdbcClient
        .sql(SQL_UPDATE)
        .param("id", horse.id())
        .param("name", horse.name())
        .param("description", horse.description())
        .param("date_of_birth", horse.dateOfBirth())
        .param("sex", horse.sex().toString())
        .param("image", image)
        .param("owner_id", horse.ownerId())
        .update();

    if (updated == 0) {
      throw new NotFoundException(
            "Could not update horse with ID " + horse.id() + ", because it does not exist"
        );
    }

    return new Horse(
            horse.id(),
            horse.name(),
            horse.description(),
            horse.dateOfBirth(),
            horse.sex(),
            image,
            horse.ownerId());
  }


  private Horse mapRow(ResultSet result, int rownum) throws SQLException {
    return new Horse(
            result.getLong("id"),
            result.getString("name"),
            result.getString("description"),
            result.getDate("date_of_birth").toLocalDate(),
            Sex.valueOf(result.getString("sex")),
            result.getBytes("image"),
            result.getObject("owner_id", Long.class));
  }
}
