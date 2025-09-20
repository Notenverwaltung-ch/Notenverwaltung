import {Routes} from '@angular/router';
import {authGuard} from './guards/auth.guard';
import {adminGuard} from './guards/admin.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () => import('./components/welcome/welcome.component').then(m => m.WelcomeComponent)
  },
  {
    path: 'welcome',
    loadComponent: () => import('./components/welcome/welcome.component').then(m => m.WelcomeComponent)
  },
  {path: 'login', loadComponent: () => import('./components/login/login.component').then(m => m.LoginComponent)},
  {
    path: 'register',
    loadComponent: () => import('./components/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'access',
    loadComponent: () => import('./components/access-prompt/access-prompt.component').then(m => m.AccessPromptComponent)
  },
  {path: 'home', redirectTo: 'welcome', pathMatch: 'full'},
  {
    path: 'grades',
    canActivate: [authGuard],
    loadComponent: () => import('./components/grades/grades.component').then(m => m.GradesComponent)
  },
  {
    path: 'users',
    canActivate: [adminGuard],
    loadComponent: () => import('./components/users/users.component').then(m => m.UsersComponent)
  },
  {path: '**', redirectTo: 'welcome'}
];
