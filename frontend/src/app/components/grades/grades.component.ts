import {Component, inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {GradeDTO, GradeService, GradeViewDTO, Page} from '../../services/grade.service';
import {AdminUserService, UserDTO} from '../../services/admin-user.service';
import {TestDTO as TestItemDTO, TestService} from '../../services/test.service';
import {AuthService} from '../../services/auth.service';

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
  private auth = inject(AuthService);

  page: Page<GradeViewDTO> | null = null;
  pageIndex = 0;
  size = 10;
  sortField: string | null | undefined;
  sortDir: 'asc' | 'desc' = 'asc';
  loading = false;
  listError: string | null = null;

  isAdmin: boolean | null = null;
  currentUsername: string | null = null;
  showOnlyOwn = false;

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
    this.isAdmin = this.auth.isAdmin();
    this.currentUsername = this.auth.getUsername();
    this.loadOptions();
  }

  private loadOptions(): void {
    this.optionsLoading = true;
    this.optionsError = null;

    if (this.isAdmin) {
      this.users.listActive(0, 1000, 'username', 'asc').subscribe({
        next: (p) => {
          this.students = p.content;
          this.optionsLoading = false;
          this.form.controls.studentId.addValidators(Validators.required);
          this.form.controls.studentId.updateValueAndValidity();
          this.load();
        },
        error: (err) => {
          this.isAdmin = false;
          this.optionsLoading = false;
          this.form.controls.studentId.clearValidators();
          this.form.controls.studentId.updateValueAndValidity();
          this.load();
        }
      });
    } else {
      this.optionsLoading = false;
      this.form.controls.studentId.clearValidators();
      this.form.controls.studentId.updateValueAndValidity();
      this.load();
    }

    this.tests.list(0, 1000, 'name', 'asc').subscribe({
      next: (p) => {
        this.testsList = p.content;
      },
      error: (err) => {
        this.optionsError = err?.error?.message ?? 'Laden der Tests fehlgeschlagen';
      }
    });
  }

  load(): void {
    this.loading = true;
    this.listError = null;
    const sortArg = this.sortField ? {field: this.sortField, dir: this.sortDir} as const : undefined;
    const caller = (this.isAdmin && this.showOnlyOwn) ? this.grades.listViewOwn(this.pageIndex, this.size, sortArg as any)
      : this.grades.listView(this.pageIndex, this.size, sortArg as any);
    caller.subscribe({
      next: (p) => {
        this.page = p;
        this.loading = false;
      },
      error: (err) => {
        this.listError = err?.error?.message ?? 'Laden der Noten fehlgeschlagen';
        this.loading = false;
      }
    });
  }

  prev() {
    if ((this.page?.number ?? 0) > 0) {
      this.pageIndex--;
      this.load();
    }
  }

  next() {
    if ((this.page?.number ?? 0) < (this.page?.totalPages ?? 1) - 1) {
      this.pageIndex++;
      this.load();
    }
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
    this.error = null;
    this.success = null;
    const dto = this.form.getRawValue();
    const base: any = {
      testId: dto.testId ? dto.testId : undefined,
      value: dto.value,
      weight: dto.weight,
      comment: dto.comment
    };
    const payload: GradeDTO = this.isAdmin ? {...base, studentId: dto.studentId} : {...base, studentId: '' as any};
    this.grades.create(payload).subscribe({
      next: () => {
        this.adding = false;
        this.success = 'Note hinzugefügt';
        this.form.reset({studentId: '', testId: '', value: 0, weight: 1, comment: ''});
        this.pageIndex = 0;
        this.load();
      },
      error: (err) => {
        this.adding = false;
        this.error = err?.error?.message ?? 'Hinzufügen der Note fehlgeschlagen';
      }
    });
  }

  onDelete(g: GradeViewDTO) {
    if (!g.id) return;
    if (!confirm('Diese Note löschen?')) return;
    this.deletingId = g.id;
    this.grades.delete(g.id).subscribe({
      next: () => {
        this.deletingId = null;
        this.load();
      },
      error: (err) => {
        this.deletingId = null;
        const msg = err?.status === 403 ? 'Du kannst nur deine eigenen Noten löschen.' : 'Löschen fehlgeschlagen';
        alert(msg);
      }
    });
  }

  isOwner(g: GradeViewDTO): boolean {
    return !!g.studentUsername && !!this.currentUsername && g.studentUsername === this.currentUsername;
  }
}
