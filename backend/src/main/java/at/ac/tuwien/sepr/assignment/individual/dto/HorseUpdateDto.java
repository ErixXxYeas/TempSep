package at.ac.tuwien.sepr.assignment.individual.dto;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Blob;
import java.time.LocalDate;

/**
 * Represents a Data Transfer Object (DTO) for updating horse details.
 * This record encapsulates all necessary fields for updating a horse entry.
 */
public record HorseUpdateDto(
    Long id,
    String name,
    String description,
    LocalDate dateOfBirth,
    Sex sex,
    Long ownerId,
    Long parentId1,
    Long parentId2
) {


}
