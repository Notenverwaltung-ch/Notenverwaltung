import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { LoginComponent } from '../login/login.component';

@Component({
  selector: 'nv-access-prompt',
  standalone: true,
  imports: [CommonModule, RouterLink, LoginComponent],
  templateUrl: './access-prompt.component.html',
  styleUrls: ['./access-prompt.component.scss']
})
export class AccessPromptComponent {
}
