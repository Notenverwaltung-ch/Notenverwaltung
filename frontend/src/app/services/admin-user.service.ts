import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {AuthService} from './auth.service';

export interface UserDTO {
  id: string;
  username: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  dateOfBirth?: string;
  active?: boolean;
  roles?: string[];
  createdAt?: string;
  updatedAt?: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({providedIn: 'root'})
export class AdminUserService {
  private http = inject(HttpClient);
  private auth = inject(AuthService);
  private baseUrl = environment.apiBaseUrl;

  private authHeaders(): HttpHeaders {
    const token = this.auth.getToken();
    let headers = new HttpHeaders({'Content-Type': 'application/json'});
    if (token) headers = headers.set('Authorization', `Bearer ${token}`);
    return headers;
  }

  listActive(page = 0, size = 50, sortField: string = 'username', sortDir: 'asc' | 'desc' = 'asc'): Observable<Page<UserDTO>> {
    let params = new HttpParams().set('page', page).set('size', size).append('sort', `${sortField},${sortDir}`);
    return this.http.get<Page<UserDTO>>(`${this.baseUrl}/admin/users/active`, {headers: this.authHeaders(), params});
  }

  list(page = 0, size = 50, sortField: string = 'username', sortDir: 'asc' | 'desc' = 'asc'): Observable<Page<UserDTO>> {
    let params = new HttpParams().set('page', page).set('size', size).append('sort', `${sortField},${sortDir}`);
    return this.http.get<Page<UserDTO>>(`${this.baseUrl}/admin/users`, {headers: this.authHeaders(), params});
  }

  create(username: string, password: string, roles: string[], profile?: Partial<Pick<UserDTO, 'firstName' | 'lastName' | 'email' | 'dateOfBirth' | 'active'>>): Observable<UserDTO> {
    const body = {username, password, roles, ...(profile || {})};
    return this.http.post<UserDTO>(`${this.baseUrl}/admin/users`, body, {headers: this.authHeaders()});
  }

  changePassword(username: string, newPassword: string): Observable<void> {
    const body = {newPassword};
    return this.http.put<void>(`${this.baseUrl}/admin/users/${encodeURIComponent(username)}/password`, body, {headers: this.authHeaders()});
  }

  setActive(username: string, active: boolean): Observable<UserDTO> {
    const params = new HttpParams().set('active', active);
    return this.http.put<UserDTO>(`${this.baseUrl}/admin/users/${encodeURIComponent(username)}/active`, null, {
      headers: this.authHeaders(),
      params
    });
  }

  grantRole(username: string, role: string): Observable<UserDTO> {
    const body = {role};
    return this.http.post<UserDTO>(`${this.baseUrl}/admin/users/${encodeURIComponent(username)}/roles`, body, {headers: this.authHeaders()});
  }

  revokeRole(username: string, role: string): Observable<UserDTO> {
    return this.http.delete<UserDTO>(`${this.baseUrl}/admin/users/${encodeURIComponent(username)}/roles/${encodeURIComponent(role)}`, {headers: this.authHeaders()});
  }

  delete(username: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/admin/users/${encodeURIComponent(username)}`, {headers: this.authHeaders()});
  }
}
