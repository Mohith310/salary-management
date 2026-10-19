import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'employees' },
  {
    path: 'employees',
    title: 'Employees',
    loadComponent: () => import('./features/employees/employee-list').then((m) => m.EmployeeList),
  },
  {
    path: 'employees/new',
    title: 'Add employee',
    loadComponent: () => import('./features/employees/employee-form').then((m) => m.EmployeeForm),
  },
  {
    path: 'employees/:id/edit',
    title: 'Edit employee',
    loadComponent: () => import('./features/employees/employee-form').then((m) => m.EmployeeForm),
  },
  {
    path: 'insights',
    title: 'Pay insights',
    loadComponent: () => import('./features/insights/insights').then((m) => m.Insights),
  },
  { path: '**', redirectTo: 'employees' },
];
