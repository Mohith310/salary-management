import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { EmployeeService } from '../../core/employee.service';
import { CountryCurrency, EmployeeRequest, FilterOptions, ProblemDetail } from '../../core/models';
import { matchOptions, notInFuture, todayIso } from '../../core/validators';

@Component({
  selector: 'app-employee-form',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatProgressBarModule,
  ],
  templateUrl: './employee-form.html',
  styleUrl: './employee-form.scss',
})
export class EmployeeForm implements OnInit {
  /** Bound from the route (:id). Absent when adding a new employee. */
  readonly id = input<string>();

  private readonly employees = inject(EmployeeService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly form = this.fb.group({
    fullName: ['', [Validators.required, Validators.maxLength(150)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(200)]],
    jobTitle: ['', [Validators.required, Validators.maxLength(100)]],
    department: ['', [Validators.required, Validators.maxLength(100)]],
    country: ['', Validators.required],
    salary: [null as number | null, [Validators.required, Validators.min(0.01)]],
    hireDate: ['', [Validators.required, notInFuture()]],
  });

  protected readonly today = todayIso();
  protected readonly countries = signal<CountryCurrency[]>([]);
  protected readonly options = signal<FilterOptions>({ countries: [], departments: [], jobTitles: [] });
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly serverError = signal<string | null>(null);

  protected readonly isEdit = computed(() => !!this.id());

  private readonly countryValue = toSignal(this.form.controls.country.valueChanges, { initialValue: '' });
  private readonly jobTitleText = toSignal(this.form.controls.jobTitle.valueChanges, { initialValue: '' });
  private readonly departmentText = toSignal(this.form.controls.department.valueChanges, { initialValue: '' });

  /** Currency of the chosen country, shown next to the salary so nobody types dollars into a rupee field. */
  protected readonly currency = computed(
    () => this.countries().find((c) => c.country === this.countryValue())?.currency ?? '',
  );
  protected readonly jobTitleOptions = computed(() => matchOptions(this.options().jobTitles, this.jobTitleText()));
  protected readonly departmentOptions = computed(() => matchOptions(this.options().departments, this.departmentText()));

  ngOnInit(): void {
    const id = this.id();
    forkJoin({
      countries: this.employees.countries(),
      options: this.employees.filterOptions(),
    }).subscribe({
      next: ({ countries, options }) => {
        this.countries.set(countries);
        this.options.set(options);
        if (id) {
          this.loadEmployee(Number(id));
        } else {
          this.loading.set(false);
        }
      },
      error: () => this.failToLoad(),
    });
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const request: EmployeeRequest = {
      fullName: value.fullName.trim(),
      email: value.email.trim(),
      jobTitle: value.jobTitle.trim(),
      department: value.department.trim(),
      country: value.country,
      salary: value.salary as number,
      hireDate: value.hireDate,
    };

    this.saving.set(true);
    this.serverError.set(null);
    const id = this.id();
    const save$ = id ? this.employees.update(Number(id), request) : this.employees.create(request);
    save$.subscribe({
      next: (saved) => {
        this.snackBar.open(`${saved.fullName} ${id ? 'updated' : 'added'}`, 'Dismiss', { duration: 4000 });
        void this.router.navigate(['/employees']);
      },
      error: (error: HttpErrorResponse) => {
        this.saving.set(false);
        this.showServerError(error);
      },
    });
  }

  private loadEmployee(id: number): void {
    this.employees.get(id).subscribe({
      next: (employee) => {
        this.form.patchValue({
          fullName: employee.fullName,
          email: employee.email,
          jobTitle: employee.jobTitle,
          department: employee.department,
          country: employee.country,
          salary: employee.salary,
          hireDate: employee.hireDate,
        });
        this.loading.set(false);
      },
      error: () => this.failToLoad('That employee could not be found.'),
    });
  }

  private failToLoad(message = 'Could not load the form data. Please try again.'): void {
    this.loading.set(false);
    this.serverError.set(message);
  }

  /** Puts server-side validation messages next to the field they belong to. */
  private showServerError(error: HttpErrorResponse): void {
    const problem = (error.error ?? {}) as ProblemDetail;

    if (error.status === 409) {
      this.setFieldError('email', problem.detail ?? 'This email is already in use');
      return;
    }
    if (error.status === 400 && problem.fieldErrors) {
      const unmatched: string[] = [];
      for (const [field, message] of Object.entries(problem.fieldErrors)) {
        if (!this.setFieldError(field, message)) {
          unmatched.push(message);
        }
      }
      if (unmatched.length > 0) {
        this.serverError.set(unmatched.join(' '));
      }
      return;
    }
    this.serverError.set(problem.detail ?? 'Something went wrong while saving. Please try again.');
  }

  private setFieldError(field: string, message: string): boolean {
    const control = this.form.get(field);
    if (!control) {
      return false;
    }
    control.setErrors({ server: message });
    control.markAsTouched();
    return true;
  }
}
