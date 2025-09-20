import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'nv-home',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container">
      <h1>Welcome</h1>
      <p>You are logged in.</p>
    </div>
  `
})
export class HomeComponent {}
