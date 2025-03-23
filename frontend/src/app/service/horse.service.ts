import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {map, Observable} from 'rxjs';
import {environment} from 'src/environments/environment';
import {Horse, HorseCreate} from '../dto/horse';
import {formatIsoDate} from "../utils/date-helper";
import {Sex} from "../dto/sex";


const baseUri = environment.backendUrl + '/horses';

@Injectable({
  providedIn: 'root'
})
export class HorseService {

  constructor(
    private http: HttpClient
  ) {}

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
    return this.http.delete<Horse>(`${baseUri}/${id}`);
  }

  /**
   * Create a new horse in the system.
   *
   * @param horse the data for the horse that should be created
   * @param image
   * @return an Observable for the created horse
   */
  create(horse: HorseCreate, image: File | null): Observable<Horse> {
    console.log(horse);
    // Cast the object to any, so that we can circumvent the type checker.
    // We _need_ the date to be a string here, and just passing the object with the
    // “type error” to the HTTP client is unproblematic
    (horse as any).dateOfBirth = formatIsoDate(horse.dateOfBirth);
    const formData = new FormData();
    formData.append('horse', JSON.stringify(horse))
    if (image != null) {
      formData.append('image', image);
    }

    return this.http.post<Horse>(
      baseUri,
      formData
    )
  }

  update(horse: HorseCreate, image: File | null, id: number): Observable<Horse> {
    console.log(horse);
    // Cast the object to any, so that we can circumvent the type checker.
    // We _need_ the date to be a string here, and just passing the object with the
    // “type error” to the HTTP client is unproblematic
    (horse as any).dateOfBirth = formatIsoDate(horse.dateOfBirth);
    const formData = new FormData();
    formData.append('horse', JSON.stringify(horse))
    if (image != null) {
      formData.append('image', image);
    }

    return this.http.put<Horse>(
      `${baseUri}/${id}`,
      formData
    ).pipe(
      map(this.fixHorseDate)
    );
  }

  public searchByParams(
    name?: string,
    description?: string,
    sex?: Sex,
    bornBefore?: string,
    dateOfBirth?: string,
    limitTo?: number
  ): Observable<Horse[]> {
    let params = new HttpParams();

    if (name) {
      params = params.set('name', name.trim());
    }
    if (description) {
      params = params.set('description', description.trim());
    }
    if (sex) {
      params = params.set('sex', sex);
    }
    if (bornBefore) {
      params = params.set('bornBefore', bornBefore);
    }
    if (dateOfBirth) {
      params = params.set('dateOfBirth', dateOfBirth);
    }
    if (limitTo !== undefined) {
      params = params.set('maxAmount', limitTo.toString());
    }

    return this.http.get<Horse[]>(baseUri, { params });
  }


  private fixHorseDate(horse: Horse): Horse {
    // Parse the string to a Date
    horse.dateOfBirth = new Date(horse.dateOfBirth as unknown as string);
    return horse;
  }

}
