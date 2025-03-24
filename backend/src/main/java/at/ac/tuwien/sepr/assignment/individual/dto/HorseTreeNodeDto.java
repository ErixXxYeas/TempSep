package at.ac.tuwien.sepr.assignment.individual.dto;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;

import java.time.LocalDate;

/**
 * Represents a Data Transfer Object (DTO) for horse tree nodes.
 * This record provides all necessary details about a horse for a tree.
 */
public record HorseTreeNodeDto(
  Long id,
  String name,
  LocalDate dateOfBirth,
  Sex sex,
  HorseTreeNodeDto parent1,
  HorseTreeNodeDto parent2
)
{

        }
