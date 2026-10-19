import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatToolbarModule, MatButtonModule, MatIconModule],
  template: `
    <mat-toolbar class="topbar">
      <span class="brand"><mat-icon>payments</mat-icon> ACME Salary Manager</span>
      <nav>
        <a mat-button routerLink="/employees" routerLinkActive="active">Employees</a>
        <a mat-button routerLink="/insights" routerLinkActive="active">Insights</a>
      </nav>
    </mat-toolbar>
    <main>
      <router-outlet />
    </main>
  `,
  styles: `
    .topbar {
      position: sticky;
      top: 0;
      z-index: 10;
      gap: 32px;
      border-bottom: 1px solid var(--mat-sys-outline-variant);
      background: var(--mat-sys-surface);
    }
    .brand {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      font-weight: 600;
      letter-spacing: -0.01em;
    }
    nav a.active {
      background: var(--mat-sys-secondary-container);
      color: var(--mat-sys-on-secondary-container);
    }
  `,
})
export class App {}
