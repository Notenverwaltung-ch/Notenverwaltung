import {Component, inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {AdminUserService, Page, UserDTO} from '../../services/admin-user.service';
import {AuthService} from '../../services/auth.service';

@Component({
  selector: 'nv-users',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss']
})
export class UsersComponent implements OnInit {
  private fb = inject(FormBuilder);
  private admin = inject(AdminUserService);
  public auth = inject(AuthService);

  page: Page<UserDTO> | null = null;
  pageIndex = 0;
  size = 10;
  sortField: string | null | undefined = 'username';
  sortDir: 'asc' | 'desc' = 'asc';
  loading = false;
  listError: string | null = null;

  // Create/Edit
  creating = false;
  editingUser: UserDTO | null = null;
  deletingUsername: string | null = null;
  error: string | null = null;
  success: string | null = null;


  // Forms
  createForm = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    firstName: [''],
    lastName: [''],
    email: [''],
    dateOfBirth: [''],
    roles: ['ROLE_USER']
  });

  editForm = this.fb.nonNullable.group({
    password: [''],
    active: [true],
    roleToGrant: ['ROLE_USER']
  });

  ngOnInit(): void {
    if (!this.auth.isAdmin()) {
      this.listError = 'Nur Administratoren können Benutzer verwalten.';
      return;
    }
    this.load();
  }

  load(): void {
    this.loading = true;
    this.listError = null;
    const sortArg = this.sortField ? {field: this.sortField, dir: this.sortDir} as const : undefined;
    this.admin.list(this.pageIndex, this.size, this.sortField ?? 'username', this.sortDir).subscribe({
      next: (p) => {
        this.page = p;
        this.loading = false;
      },
      error: (err) => {
        this.listError = err?.error?.message ?? 'Laden der Benutzer fehlgeschlagen';
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
      if (this.sortDir === 'asc') this.sortDir = 'desc'; else {
        this.sortField = null;
        this.sortDir = 'asc';
      }
    }
    this.pageIndex = 0;
    this.load();
  }


  onCreate() {
    if (this.createForm.invalid) return;
    this.creating = true;
    this.error = null;
    this.success = null;
    const rolesRaw = this.createForm.value.roles || 'ROLE_USER';
    const roles = String(rolesRaw).split(',').map(r => r.trim()).filter(Boolean);
    const payload: any = {
      username: this.createForm.value.username!,
      password: this.createForm.value.password!,
      roles,
      firstName: this.createForm.value.firstName || undefined,
      lastName: this.createForm.value.lastName || undefined,
      email: this.createForm.value.email || undefined,
      dateOfBirth: this.createForm.value.dateOfBirth || undefined,
      active: true,
    };
    fetch(`${(window as any).env?.apiBaseUrl || ''}`).toString(); // noop to keep env import tree-shaken safe
    this.adminCreate(payload);
  }

  private adminCreate(payload: {
    username: string;
    password: string;
    roles: string[];
    firstName?: string;
    lastName?: string;
    email?: string;
    dateOfBirth?: string;
    active?: boolean
  }) {
    this.admin.create(payload.username, payload.password, payload.roles, {
      firstName: payload.firstName,
      lastName: payload.lastName,
      email: payload.email,
      dateOfBirth: payload.dateOfBirth,
      active: payload.active
    }).subscribe({
      next: () => {
        this.creating = false;
        this.success = 'Benutzer erstellt';
        this.error = null;
        this.createForm.reset({username: '', password: '', roles: 'ROLE_USER'});
        this.pageIndex = 0;
        this.load();
      },
      error: (err) => {
        this.creating = false;
        this.error = err?.error?.message ?? 'Erstellen fehlgeschlagen';
      }
    });
  }

  onChangePassword(u: UserDTO) {
    const pwd = prompt(`Neues Passwort für ${u.username}:`);
    if (!pwd) return;
    this.admin.changePassword(u.username, pwd).subscribe({
      next: () => {
        alert('Passwort geändert');
      },
      error: () => {
        alert('Passwort ändern fehlgeschlagen');
      }
    });
  }

  onToggleActive(u: UserDTO) {
    const newActive = !u.active;
    this.admin.setActive(u.username, newActive).subscribe({
      next: () => {
        if (u) u.active = newActive;
      },
      error: () => {
        alert('Aktualisieren fehlgeschlagen');
      }
    });
  }

  onGrantRole(u: UserDTO) {
    const role = prompt('Rolle hinzufügen (z.B. ROLE_ADMIN):', 'ROLE_USER');
    if (!role) return;
    this.admin.grantRole(u.username, role).subscribe({
      next: (updated) => {
        u.roles = updated.roles;
      },
      error: () => {
        alert('Rolle hinzufügen fehlgeschlagen');
      }
    });
  }

  onRevokeRole(u: UserDTO, role: string) {
    if (!confirm(`Rolle ${role} von ${u.username} entfernen?`)) return;
    this.admin.revokeRole(u.username, role).subscribe({
      next: (updated) => {
        u.roles = updated.roles;
      },
      error: () => {
        alert('Rolle entfernen fehlgeschlagen');
      }
    });
  }

  onEditRoles(u: UserDTO) {
    const current = (u.roles || []).join(', ');
    const input = prompt(`Rollen für ${u.username} bearbeiten (kommagetrennt)`, current);
    if (input === null) return; // cancel
    // parse roles
    const newRoles = Array.from(new Set(String(input)
      .split(',')
      .map(r => r.trim())
      .filter(r => !!r)));

    const oldRoles = Array.from(new Set(u.roles || []));

    // determine diffs
    const toAdd = newRoles.filter(r => !oldRoles.includes(r));
    const toRemove = oldRoles.filter(r => !newRoles.includes(r));

    if (toAdd.length === 0 && toRemove.length === 0) {
      return; // nothing to do
    }

    // Execute sequentially to keep it simple and avoid extra imports
    const doNext = () => {
      if (toRemove.length > 0) {
        const role = toRemove.shift()!;
        this.admin.revokeRole(u.username, role).subscribe({
          next: (updated) => {
            u.roles = updated.roles;
            doNext();
          },
          error: () => {
            alert('Rolle entfernen fehlgeschlagen');
          }
        });
        return;
      }
      if (toAdd.length > 0) {
        const role = toAdd.shift()!;
        this.admin.grantRole(u.username, role).subscribe({
          next: (updated) => {
            u.roles = updated.roles;
            doNext();
          },
          error: () => {
            alert('Rolle hinzufügen fehlgeschlagen');
          }
        });
        return;
      }
      // done
      alert('Rollen aktualisiert');
    };

    doNext();
  }

  onDelete(u: UserDTO) {
    if (!confirm(`Benutzer ${u.username} löschen?`)) return;
    this.deletingUsername = u.username;
    this.admin.delete(u.username).subscribe({
      next: () => {
        this.deletingUsername = null;
        this.load();
      },
      error: () => {
        this.deletingUsername = null;
        alert('Löschen fehlgeschlagen');
      }
    });
  }
}
