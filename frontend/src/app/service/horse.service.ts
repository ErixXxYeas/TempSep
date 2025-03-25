import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {map, Observable} from 'rxjs';
import {environment} from 'src/environments/environment';
import {Horse, HorseCreate, HorseNode, HorseSearch} from '../dto/horse';
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


  getById(id: number): Observable<Horse>{
    return this.http.get<Horse>(`${baseUri}/${id}`).pipe(
      map(this.fixHorseDate)
    );
  }

  getByIdForTree(id: number, generations: number): Observable<HorseNode>{
    return this.http.get<HorseNode>(`${baseUri}/${id}/familytree?generations=${generations}`).pipe(
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

  public searchByParams(searchParams: HorseSearch
  ): Observable<Horse[]> {
    let params = new HttpParams();

    if (searchParams.name) {
      params = params.set('name', searchParams.name.trim());
    }
    if (searchParams.description) {
      params = params.set('description', searchParams.description.trim());
    }
    if (searchParams.sex) {
      params = params.set('sex',searchParams.sex);
    }
    if (searchParams.bornBefore) {
      params = params.set('bornBefore', searchParams.bornBefore);
    }
    if (searchParams.dateOfBirth) {
      params = params.set('dateOfBirth', formatIsoDate(searchParams.dateOfBirth));
    }
    if(searchParams.ownerName){
      const name = searchParams.ownerName.split(' ')
      params = params.set('ownerFirstName', name[0])
      params = params.set('ownerLastName', name[1])
    }
    if (searchParams.limit !== undefined) {
      params = params.set('maxAmount', searchParams.limit.toString());
    }

    console.log(searchParams.limit)
    return this.http.get<Horse[]>(baseUri, { params });
  }


  private fixHorseDate(horse: Horse): Horse {
    // Parse the string to a Date
    horse.dateOfBirth = new Date(horse.dateOfBirth as unknown as string);
    return horse;
  }

}
