import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {AuthService} from './auth.service';

export interface TestDTO {
  id: string;
  name: string;
  comment?: string;
  date?: string; // ISO date
  semesterSubjectId?: string;
  classId?: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({providedIn: 'root'})
export class TestService {
  private http = inject(HttpClient);
  private auth = inject(AuthService);
  private baseUrl = environment.apiBaseUrl;

  private authHeaders(): HttpHeaders {
    const token = this.auth.getToken();
    let headers = new HttpHeaders({'Content-Type': 'application/json'});
    if (token) headers = headers.set('Authorization', `Bearer ${token}`);
    return headers;
  }

  list(page = 0, size = 50, sortField: string = 'name', sortDir: 'asc' | 'desc' = 'asc'): Observable<Page<TestDTO>> {
    let params = new HttpParams().set('page', page).set('size', size).append('sort', `${sortField},${sortDir}`);
    return this.http.get<Page<TestDTO>>(`${this.baseUrl}/tests`, {headers: this.authHeaders(), params});
  }
}
