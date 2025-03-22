import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Owner } from '../dto/owner';
import {Horse} from "../dto/horse";

const baseUri = environment.backendUrl + '/owners';

@Injectable({
  providedIn: 'root'
})
export class OwnerService {

  constructor(
    private http: HttpClient,
  ) { }

  public searchByName(name?: string, limitTo?: number): Observable<Owner[]> {
    const params = new HttpParams()


    if (name) {
      params.set('name', name);
    }
    if (limitTo !== undefined && limitTo > 0) {
        params.set('maxAmount', limitTo);
    }

    return this.http.get<Owner[]>(baseUri, { params });
  }

  create(owner: Owner): Observable<Owner>{
    return this.http.post<Owner>(baseUri,owner)
  }

  deleteById(id: number | undefined){
    return this.http.delete<Horse>(`${baseUri}/${id}`)
  }


}
