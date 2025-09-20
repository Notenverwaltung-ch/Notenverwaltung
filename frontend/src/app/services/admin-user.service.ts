import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

export interface UserDTO {
  id: string;
  username: string;
  active?: boolean;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({ providedIn: 'root' })
export class AdminUserService {
  private http = inject(HttpClient);
  private auth = inject(AuthService);
  private baseUrl = environment.apiBaseUrl;

  private authHeaders(): HttpHeaders {
    const token = this.auth.getToken();
    let headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    if (token) headers = headers.set('Authorization', `Bearer ${token}`);
    return headers;
  }

  listActive(page = 0, size = 50, sortField: string = 'username', sortDir: 'asc' | 'desc' = 'asc'): Observable<Page<UserDTO>> {
    let params = new HttpParams().set('page', page).set('size', size).append('sort', `${sortField},${sortDir}`);
    return this.http.get<Page<UserDTO>>(`${this.baseUrl}/admin/users/active`, { headers: this.authHeaders(), params });
  }

  list(page = 0, size = 50, sortField: string = 'username', sortDir: 'asc' | 'desc' = 'asc'): Observable<Page<UserDTO>> {
    let params = new HttpParams().set('page', page).set('size', size).append('sort', `${sortField},${sortDir}`);
    return this.http.get<Page<UserDTO>>(`${this.baseUrl}/admin/users`, { headers: this.authHeaders(), params });
  }
}
