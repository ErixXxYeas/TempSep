package at.ac.tuwien.sepr.assignment.individual.entity;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;

import java.sql.Blob;
import java.time.LocalDate;

/**
 * Represents a horse in the persistent data store.
 */
public record Horse(
    Long id,
    String name,
    String description,
    LocalDate dateOfBirth,
    Sex sex,
    Blob image,
    Long ownerId
) {
}
