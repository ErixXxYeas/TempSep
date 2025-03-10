import {Owner} from './owner';
import {Sex} from './sex';

export interface Horse {
  image?: string;
  id?: number;
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
  image?: File;
  ownerId?: number;
}

export function convertFromHorseToCreate(horse: Horse, image: File | null): HorseCreate {
  return {
    name: horse.name,
    description: horse.description,
    dateOfBirth: horse.dateOfBirth,
    sex: horse.sex,
    image: image || undefined,
    ownerId: horse.owner?.id,
  };
}

