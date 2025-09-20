import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { GradeService, GradeDTO, Page, GradeViewDTO } from '../../services/grade.service';
import { AdminUserService, UserDTO } from '../../services/admin-user.service';
import { TestService, TestDTO as TestItemDTO } from '../../services/test.service';

@Component({
  selector: 'nv-grades',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './grades.component.html',
  styleUrls: ['./grades.component.scss']
})
export class GradesComponent implements OnInit {
  private fb = inject(FormBuilder);
  private grades = inject(GradeService);
  private users = inject(AdminUserService);
  private tests = inject(TestService);

  page: Page<GradeViewDTO> | null = null;
  pageIndex = 0;
  size = 10;
  sortField: string | null | undefined;
  sortDir: 'asc' | 'desc' = 'asc';
  loading = false;
  listError: string | null = null;

  form = this.fb.nonNullable.group({
    studentId: ['', Validators.required],
    testId: [''],
    value: [0 as number, [Validators.required]],
    weight: [1 as number],
    comment: ['']
  });
  adding = false;
  deletingId: string | null = null;
  error: string | null = null;
  success: string | null = null;

  students: UserDTO[] = [];
  testsList: TestItemDTO[] = [];
  optionsLoading = false;
  optionsError: string | null = null;

  ngOnInit(): void {
    this.loadOptions();
    this.load();
  }

  private loadOptions(): void {
    this.optionsLoading = true;
    this.optionsError = null;
    // Load active users (students) and tests
    this.users.listActive(0, 1000, 'username', 'asc').subscribe({
      next: (p) => { this.students = p.content; this.optionsLoading = false; },
      error: (err) => { this.optionsLoading = false; this.optionsError = err?.error?.message ?? 'Failed to load users'; }
    });
    this.tests.list(0, 1000, 'name', 'asc').subscribe({
      next: (p) => { this.testsList = p.content; },
      error: (err) => { this.optionsError = err?.error?.message ?? 'Failed to load tests'; }
    });
  }

  load(): void {
    this.loading = true;
    this.listError = null;
    const sortArg = this.sortField ? { field: this.sortField, dir: this.sortDir } as const : undefined;
    this.grades.listView(this.pageIndex, this.size, sortArg as any).subscribe({
      next: (p) => { this.page = p; this.loading = false; },
      error: (err) => { this.listError = err?.error?.message ?? 'Failed to load grades'; this.loading = false; }
    });
  }

  prev() {
    if ((this.page?.number ?? 0) > 0) { this.pageIndex--; this.load(); }
  }

  next() {
    if ((this.page?.number ?? 0) < (this.page?.totalPages ?? 1) - 1) { this.pageIndex++; this.load(); }
  }

  toggleSort(field: string) {
    if (this.sortField !== field) {
      this.sortField = field;
      this.sortDir = 'asc';
    } else {
      if (this.sortDir === 'asc') {
        this.sortDir = 'desc';
      } else {
        this.sortField = null; // clear sorting
        this.sortDir = 'asc';
      }
    }
    this.pageIndex = 0;
    this.load();
  }

  onAdd() {
    if (this.form.invalid) return;
    this.adding = true;
    this.error = null; this.success = null;
    const dto = this.form.getRawValue();
    const payload: GradeDTO = { ...dto, testId: dto.testId ? dto.testId : undefined } as any;
    this.grades.create(payload).subscribe({
      next: () => {
        this.adding = false;
        this.success = 'Grade added';
        this.form.reset({ studentId: '', testId: '', value: 0, weight: 1, comment: '' });
        this.pageIndex = 0;
        this.load();
      },
      error: (err) => { this.adding = false; this.error = err?.error?.message ?? 'Failed to add grade'; }
    });
  }

  onDelete(g: GradeDTO) {
    if (!g.id) return;
    if (!confirm('Delete this grade?')) return;
    this.deletingId = g.id;
    this.grades.delete(g.id).subscribe({
      next: () => { this.deletingId = null; this.load(); },
      error: () => { this.deletingId = null; alert('Failed to delete'); }
    });
  }
}
