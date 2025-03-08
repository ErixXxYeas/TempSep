package at.ac.tuwien.sepr.assignment.individual.dto;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * Represents a Data Transfer Object (DTO) for creating a new horse entry.
 * This record encapsulates all necessary details for registering a horse.
 */
public record HorseCreateDto(
    String name,
    String description,
    LocalDate dateOfBirth,
    Sex sex,
    MultipartFile image,
    Long ownerId
) {

}
