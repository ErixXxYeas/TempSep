import {Owner} from './owner';
import {Sex} from './sex';

export interface Horse {
  image?: string;
  id?: number;
  parent1?: Horse;
  parent2?: Horse;
  name: string;
  description?: string;
  dateOfBirth: Date;
  sex: Sex;
  owner?: Owner;
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
  parentId1?: number;
  parentId2?: number;
}

export function convertFromHorseToCreate(horse: Horse): HorseCreate {
  return {
    name: horse.name,
    description: horse.description,
    dateOfBirth: horse.dateOfBirth,
    sex: horse.sex,
    ownerId: horse.owner?.id,
    parentId1: horse.parent1?.id,
    parentId2: horse.parent2?.id
  };
}

