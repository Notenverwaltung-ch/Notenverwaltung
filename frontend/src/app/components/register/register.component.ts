import {Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {AuthService} from '../../services/auth.service';

@Component({
  selector: 'nv-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  form = this.fb.nonNullable.group({
    username: ['', [Validators.required]],
    firstName: [''],
    lastName: [''],
    email: ['', [Validators.email]],
    dateOfBirth: [''],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  loading = false;
  error: string | null = null;

  submit() {
    this.error = null;
    if (this.form.invalid) return;
    this.loading = true;
    const {username, email, password, firstName, lastName, dateOfBirth} = this.form.getRawValue();
    this.auth.register({
      username,
      email: email || undefined,
      password,
      firstName: firstName || undefined,
      lastName: lastName || undefined,
      dateOfBirth: dateOfBirth || undefined
    }).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/welcome']);
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message || 'Registrierung fehlgeschlagen.';
      }
    });
  }
}
