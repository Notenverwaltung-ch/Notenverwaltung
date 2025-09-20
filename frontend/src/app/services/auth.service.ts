import {inject, Injectable, PLATFORM_ID} from '@angular/core';
import {isPlatformBrowser} from '@angular/common';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Router} from '@angular/router';
import {environment} from '../../environments/environment';
import {Observable, tap} from 'rxjs';

export interface AuthRequestDTO {
  username: string;
  password: string;
}

export interface UserRegistrationDTO {
  username: string;
  password: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  dateOfBirth?: string;
}

export interface AuthResponseDTO {
  token?: string;
  tokenType?: string;
  expiresIn?: number;
}

@Injectable({providedIn: 'root'})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID);
  private baseUrl = environment.apiBaseUrl;
  private tokenKey = 'auth_token';

  private hasStorage(): boolean {
    return isPlatformBrowser(this.platformId) && typeof localStorage !== 'undefined';
  }

  private setToken(token: string): void {
    if (this.hasStorage()) {
      try {
        localStorage.setItem(this.tokenKey, token);
      } catch {
      }
    }
  }

  private removeToken(): void {
    if (this.hasStorage()) {
      try {
        localStorage.removeItem(this.tokenKey);
      } catch {
      }
    }
  }

  getToken(): string | null {
    if (this.hasStorage()) {
      try {
        return localStorage.getItem(this.tokenKey);
      } catch {
        return null;
      }
    }
    return null;
  }

  login(payload: AuthRequestDTO): Observable<HttpResponse<AuthResponseDTO>> {
    return this.http.post<AuthResponseDTO>(`${this.baseUrl}/public/auth/login`, payload, {observe: 'response' as const}).pipe(
      tap(response => {
        const res = response.body as AuthResponseDTO | null;
        let token: string | undefined = res?.token;
        if (!token) {
          const authHeader = response.headers.get('Authorization') || response.headers.get('authorization');
          if (authHeader && authHeader.toLowerCase().startsWith('bearer ')) {
            token = authHeader.substring(7);
          }
        }
        if (token) {
          this.setToken(token);
        }
      })
    );
  }

  register(payload: UserRegistrationDTO): Observable<HttpResponse<AuthResponseDTO>> {
    return this.http.post<AuthResponseDTO>(`${this.baseUrl}/public/auth/register`, payload, {observe: 'response' as const}).pipe(
      tap(response => {
        const res = response.body as AuthResponseDTO | null;
        let token: string | undefined = res?.token;
        if (!token) {
          const authHeader = response.headers.get('Authorization') || response.headers.get('authorization');
          if (authHeader && authHeader.toLowerCase().startsWith('bearer ')) {
            token = authHeader.substring(7);
          }
        }
        if (token) {
          this.setToken(token);
        }
      })
    );
  }

  logout(): void {
    this.removeToken();
    this.router.navigate(['/login']);
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    return !!token;
  }

  getUsername(): string | null {
    const token = this.getToken();
    if (!token) return null;
    const payload = this.decodeJwt(token);
    if (!payload) return null;
    const username = (payload['username'] as string | undefined) || (payload['sub'] as string | undefined);
    return username ?? null;
  }

  getRoles(): string[] {
    const token = this.getToken();
    if (!token) return [];
    const payload = this.decodeJwt(token);
    if (!payload) return [];
    const roles = payload['roles'];
    if (Array.isArray(roles)) {
      return roles.map(r => String(r));
    }
    if (typeof roles === 'string') return [roles];
    return [];
  }

  hasRole(role: string): boolean {
    return this.getRoles().includes(role);
  }

  isAdmin(): boolean {
    const roles = this.getRoles();
    return roles.includes('ROLE_ADMIN') || roles.includes('ADMIN');
  }

  isUser(): boolean {
    const roles = this.getRoles();
    return roles.includes('ROLE_USER') || roles.includes('USER');
  }

  private decodeJwt(token: string): Record<string, unknown> | null {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) return null;
      const base64Url = parts[1].replace(/-/g, '+').replace(/_/g, '/');
      const padded = base64Url + '='.repeat((4 - (base64Url.length % 4)) % 4);
      const json = atob(padded);
      return JSON.parse(json);
    } catch {
      return null;
    }
  }
}
