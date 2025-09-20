import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { GradeService, GradeViewDTO, Page } from '../../services/grade.service';

@Component({
  selector: 'nv-welcome',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './welcome.component.html',
  styleUrls: ['./welcome.component.scss']
})
export class WelcomeComponent implements OnInit {
  latestGrades: GradeViewDTO[] | null = null;
  loadingLatest = false;
  latestError: string | null = null;

  constructor(public auth: AuthService, private grades: GradeService) {}

  ngOnInit(): void {
    if (this.auth.isAuthenticated()) {
      this.loadLatest();
    }
  }

  get username(): string | null { return this.auth.getUsername(); }

  private loadLatest() {
    this.loadingLatest = true;
    this.latestError = null;
    this.grades.listViewOwn(0, 3, { field: 'createdOn', dir: 'desc' }).subscribe({
      next: (page: Page<GradeViewDTO>) => {
        this.latestGrades = page.content ?? [];
        this.loadingLatest = false;
      },
      error: (err) => {
        this.latestError = err?.error?.message || 'Fehler beim Laden der letzten Noten.';
        this.loadingLatest = false;
      }
    });
  }
}
