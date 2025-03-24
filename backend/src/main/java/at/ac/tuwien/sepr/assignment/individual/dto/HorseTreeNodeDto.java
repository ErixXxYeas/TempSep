package at.ac.tuwien.sepr.assignment.individual.dto;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;

import java.time.LocalDate;

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
