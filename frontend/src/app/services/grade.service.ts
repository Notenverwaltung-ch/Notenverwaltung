import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

export interface GradeDTO {
  id?: string;
  value: number;
  weight?: number;
  comment?: string;
  studentId: string;
  testId?: string; // optional
}

export interface GradeViewDTO {
  value: number;
  weight?: number;
  comment?: string;
  studentUsername?: string;
  testName?: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({ providedIn: 'root' })
export class GradeService {
  private http = inject(HttpClient);
  private auth = inject(AuthService);
  private baseUrl = environment.apiBaseUrl;

  private authHeaders(): HttpHeaders {
    const token = this.auth.getToken();
    let headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    if (token) headers = headers.set('Authorization', `Bearer ${token}`);
    return headers;
  }

  list(page = 0, size = 10, filters?: { studentId?: string; testId?: string; valueMin?: number; valueMax?: number }): Observable<Page<GradeDTO>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (filters?.studentId) params = params.set('studentId', filters.studentId);
    if (filters?.testId) params = params.set('testId', filters.testId);
    if (filters?.valueMin !== undefined) params = params.set('valueMin', filters.valueMin);
    if (filters?.valueMax !== undefined) params = params.set('valueMax', filters.valueMax);
    return this.http.get<Page<GradeDTO>>(`${this.baseUrl}/grades`, { headers: this.authHeaders(), params });
  }

  listView(page = 0, size = 10, sort?: { field: string; dir: 'asc' | 'desc' }): Observable<Page<GradeViewDTO>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (sort?.field) {
      params = params.append('sort', `${sort.field},${sort.dir ?? 'asc'}`);
    }
    return this.http.get<Page<GradeViewDTO>>(`${this.baseUrl}/grades/view`, { headers: this.authHeaders(), params });
  }

  create(dto: GradeDTO): Observable<GradeDTO> {
    return this.http.post<GradeDTO>(`${this.baseUrl}/grades`, dto, { headers: this.authHeaders() });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/grades/${id}`, { headers: this.authHeaders() });
  }
}
