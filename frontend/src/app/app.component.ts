import {Component, inject} from '@angular/core';
import {RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {CommonModule} from '@angular/common';
import {AuthService} from './services/auth.service';
import {environment} from '../environments/environment';

@Component({
  selector: 'nv-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'Notenverwaltung';
  auth = inject(AuthService);

  logout() {
    this.auth.logout();
  }

  swaggerUrl() {
    return environment.apiBaseUrl + '/public/swagger-ui/index.html';
  }
}
