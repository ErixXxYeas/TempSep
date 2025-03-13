import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {map, Observable} from 'rxjs';
import {environment} from 'src/environments/environment';
import {Horse, HorseCreate} from '../dto/horse';
import {formatIsoDate} from "../utils/date-helper";
import {Block} from "@angular/compiler";
import {Owner} from "../dto/owner";


const baseUri = environment.backendUrl + '/horses';

@Injectable({
  providedIn: 'root'
})
export class HorseService {

  constructor(
    private http: HttpClient
  ) {
  }

  /**
   * Get all horses stored in the system
   *
   * @return observable list of found horses.
   */
  getAll(): Observable<Horse[]> {
    return this.http.get<Horse[]>(baseUri)
      .pipe(
        map(horses => horses.map(this.fixHorseDate))
      );
  }


  getById(id: number): Observable<Horse>{
    return this.http.get<Horse>(`${baseUri}/${id}`).pipe(
      map(this.fixHorseDate)
    );
  }

  deleteById(id: number | undefined): Observable<Horse>{
    console.log("JO")
    console.log(id)
    return this.http.delete<Horse>(`${baseUri}/${id}`);
  }


  /**
   * Create a new horse in the system.
   *
   * @param horse the data for the horse that should be created
   * @return an Observable for the created horse
   */
  create(horse: HorseCreate): Observable<Horse> {
    console.log(horse);
    // Cast the object to any, so that we can circumvent the type checker.
    // We _need_ the date to be a string here, and just passing the object with the
    // “type error” to the HTTP client is unproblematic
    (horse as any).dateOfBirth = formatIsoDate(horse.dateOfBirth);
    const formData = new FormData();

    formData.append('name', horse.name);
    formData.append('dateOfBirth', horse.dateOfBirth.toString());
    formData.append('sex', horse.sex.toString());

    if (horse.description) {
      formData.append('description', horse.description);
    }
    if (horse.image != null) {
      formData.append('image', horse.image, horse.image.name);
    }
    if (horse.ownerId !== undefined) {
      formData.append('ownerId', horse.ownerId.toString());
    }

    if (horse.parentId1 !== undefined) {
      formData.append('parentId1', horse.parentId1.toString());
    }

    if (horse.parentId2 !== undefined) {
      formData.append('parentId2', horse.parentId2.toString());
    }

    return this.http.post<Horse>(
      baseUri,
      formData
    ).pipe(
      map(this.fixHorseDate)
    );
  }

  update(horse: HorseCreate, id: number): Observable<Horse> {
    console.log(horse);
    // Cast the object to any, so that we can circumvent the type checker.
    // We _need_ the date to be a string here, and just passing the object with the
    // “type error” to the HTTP client is unproblematic
    (horse as any).dateOfBirth = formatIsoDate(horse.dateOfBirth);
    const formData = new FormData();

    formData.append('name', horse.name);
    formData.append('dateOfBirth', horse.dateOfBirth.toString());
    formData.append('sex', horse.sex.toString());

    if (horse.description) {
      formData.append('description', horse.description);
    }
    if (horse.image) {
      formData.append('image', horse.image, horse.image.name);
    }
    if (horse.ownerId !== undefined) {
      formData.append('ownerId', horse.ownerId.toString());
    }

    if (horse.parentId1 !== undefined) {
      formData.append('parentId1', horse.parentId1.toString());
    }

    if (horse.parentId2 !== undefined) {
      formData.append('parentId2', horse.parentId2.toString());
    }

    return this.http.put<Horse>(
      `${baseUri}/${id}`,
      formData
    ).pipe(
      map(this.fixHorseDate)
    );
  }

  public searchByName(name: string, limitTo: number): Observable<Horse[]> {
    const params = new HttpParams()
      .set('name', name)
      .set('maxAmount', limitTo);
    return this.http.get<Horse[]>(baseUri, { params });
  }

  private fixHorseDate(horse: Horse): Horse {
    // Parse the string to a Date
    horse.dateOfBirth = new Date(horse.dateOfBirth as unknown as string);
    return horse;
  }

}
