import {Owner} from './owner';
import {Sex} from './sex';

export interface Horse {
  image?: string;
  id?: number;
  parent1Id?: number;
  parent2Id?: number;
  name: string;
  description?: string;
  dateOfBirth: Date;
  sex: Sex;
  owner?: Owner;
}

export interface HorseNode {
  id?: number;
  parent1?: HorseNode;
  parent2?: HorseNode;
  name: string;
  dateOfBirth: Date;
  sex: Sex;
}

export interface HorseSearch {
  name?: string;
  // TODO fill in missing fields
}

export interface HorseCreate {
  name: string;
  description?: string;
  dateOfBirth: Date;
  sex: Sex;
  ownerId?: number;
  parent1Id?: number;
  parent2Id?: number;
}

export function convertFromHorseToCreate(horse: Horse): HorseCreate {
  return {
    name: horse.name,
    description: horse.description,
    dateOfBirth: horse.dateOfBirth,
    sex: horse.sex,
    ownerId: horse.owner?.id,
    parent1Id: horse.parent1Id,
    parent2Id: horse.parent2Id
  };
}

