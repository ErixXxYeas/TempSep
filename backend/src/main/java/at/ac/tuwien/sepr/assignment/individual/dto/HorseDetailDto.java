package at.ac.tuwien.sepr.assignment.individual.dto;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;

import java.io.InputStream;
import java.time.LocalDate;

/**
 * Represents a Data Transfer Object (DTO) for detailed horse information.
 * This record provides all necessary details about a horse.
 */
public record HorseDetailDto(
    Long id,
    String name,
    String description,
    LocalDate dateOfBirth,
    Sex sex,
    boolean image,
    OwnerDto owner,
    Long parent1Id,
    Long parent2Id

) {
}
