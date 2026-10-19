import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { EMPTY, Subject, catchError, debounceTime, filter, switchMap, tap } from 'rxjs';
import { EmployeeService } from '../../core/employee.service';
import { formatMoney } from '../../core/money';
import { Employee, EmployeeFilter, FilterOptions, SortDirection } from '../../core/models';
import { ConfirmDialog } from '../../shared/confirm-dialog';

@Component({
  selector: 'app-employee-list',
  imports: [
    DatePipe,
    DecimalPipe,
    ReactiveFormsModule,
    RouterLink,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
  ],
  templateUrl: './employee-list.html',
  styleUrl: './employee-list.scss',
})
export class EmployeeList implements OnInit {
  private readonly employees = inject(EmployeeService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly reload$ = new Subject<void>();

  protected readonly columns = ['fullName', 'jobTitle', 'department', 'country', 'salary', 'hireDate', 'actions'];
  protected readonly formatMoney = formatMoney;

  protected readonly rows = signal<Employee[]>([]);
  protected readonly total = signal(0);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly options = signal<FilterOptions>({ countries: [], departments: [], jobTitles: [] });

  protected readonly page = signal(0);
  protected readonly size = signal(25);
  protected readonly sortBy = signal('fullName');
  protected readonly direction = signal<SortDirection>('asc');

  protected readonly filters = this.fb.group({
    q: '',
    country: '',
    department: '',
    jobTitle: '',
    minSalary: null as number | null,
    maxSalary: null as number | null,
  });

  constructor() {
    // switchMap drops the response of an older request if a newer one has been started.
    this.reload$
      .pipe(
        tap(() => {
          this.loading.set(true);
          this.error.set(null);
        }),
        switchMap(() =>
          this.employees
            .list({
              filter: this.currentFilter(),
              page: this.page(),
              size: this.size(),
              sortBy: this.sortBy(),
              direction: this.direction(),
            })
            .pipe(
              catchError(() => {
                this.loading.set(false);
                this.error.set('Could not load employees. Please check the connection and try again.');
                return EMPTY;
              }),
            ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((result) => {
        this.loading.set(false);
        const lastPage = Math.max(result.totalPages - 1, 0);
        if (this.page() > lastPage) {
          // e.g. the last row of the last page was deleted: step back instead of showing an empty page.
          this.page.set(lastPage);
          this.reload$.next();
          return;
        }
        this.rows.set(result.content);
        this.total.set(result.totalElements);
      });

    // Salary amounts are in local currency, so a range only makes sense once a country is chosen.
    this.setSalaryRangeEnabled(false);
    this.filters.controls.country.valueChanges.pipe(takeUntilDestroyed()).subscribe((country) => {
      this.setSalaryRangeEnabled(!!country);
    });

    this.filters.valueChanges.pipe(debounceTime(300), takeUntilDestroyed()).subscribe(() => {
      this.page.set(0);
      this.reload$.next();
    });
  }

  ngOnInit(): void {
    this.employees.filterOptions().subscribe({ next: (options) => this.options.set(options) });
    this.reload$.next();
  }

  protected onSort(sort: Sort): void {
    this.sortBy.set(sort.direction ? sort.active : 'fullName');
    this.direction.set(sort.direction === 'desc' ? 'desc' : 'asc');
    this.page.set(0);
    this.reload$.next();
  }

  protected onPage(event: PageEvent): void {
    this.page.set(event.pageIndex);
    this.size.set(event.pageSize);
    this.reload$.next();
  }

  protected clearFilters(): void {
    this.filters.reset();
  }

  protected hasActiveFilters(): boolean {
    const v = this.filters.getRawValue();
    return !!(v.q || v.country || v.department || v.jobTitle || v.minSalary !== null || v.maxSalary !== null);
  }

  protected remove(employee: Employee): void {
    this.dialog
      .open(ConfirmDialog, {
        data: {
          title: 'Delete employee?',
          message: `${employee.fullName} will be permanently removed. This cannot be undone.`,
          confirmLabel: 'Delete',
        },
      })
      .afterClosed()
      .pipe(
        filter((confirmed) => confirmed === true),
        switchMap(() => this.employees.delete(employee.id)),
      )
      .subscribe({
        next: () => {
          this.snackBar.open(`${employee.fullName} deleted`, 'Dismiss', { duration: 4000 });
          this.reload$.next();
        },
        error: () => this.snackBar.open('Could not delete the employee. Please try again.', 'Dismiss', { duration: 6000 }),
      });
  }

  private currentFilter(): EmployeeFilter {
    const v = this.filters.getRawValue();
    return {
      q: v.q.trim(),
      country: v.country,
      department: v.department,
      jobTitle: v.jobTitle,
      minSalary: v.country ? v.minSalary : null,
      maxSalary: v.country ? v.maxSalary : null,
    };
  }

  private setSalaryRangeEnabled(enabled: boolean): void {
    const { minSalary, maxSalary } = this.filters.controls;
    if (enabled) {
      minSalary.enable({ emitEvent: false });
      maxSalary.enable({ emitEvent: false });
    } else {
      minSalary.reset(null, { emitEvent: false });
      maxSalary.reset(null, { emitEvent: false });
      minSalary.disable({ emitEvent: false });
      maxSalary.disable({ emitEvent: false });
    }
  }
}
