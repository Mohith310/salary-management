import { DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTabsModule } from '@angular/material/tabs';
import { BehaviorSubject, EMPTY, catchError, forkJoin, switchMap, tap } from 'rxjs';
import { EmployeeService } from '../../core/employee.service';
import { InsightsService } from '../../core/insights.service';
import { formatMoney } from '../../core/money';
import { CountryCurrency, DistributionResponse, GroupInsight } from '../../core/models';
import { InsightsTable } from './insights-table';

export interface InsightsData {
  summary: GroupInsight;
  distribution: DistributionResponse;
  byDepartment: GroupInsight[];
  byJobTitle: GroupInsight[];
  byCountry: GroupInsight[];
}

@Component({
  selector: 'app-insights',
  imports: [DecimalPipe, MatFormFieldModule, MatSelectModule, MatTabsModule, MatProgressBarModule, InsightsTable],
  templateUrl: './insights.html',
  styleUrl: './insights.scss',
})
export class Insights implements OnInit {
  private readonly insights = inject(InsightsService);
  private readonly employees = inject(EmployeeService);
  private readonly country$ = new BehaviorSubject<string>('');

  protected readonly formatMoney = formatMoney;
  protected readonly country = signal('');
  protected readonly countries = signal<CountryCurrency[]>([]);
  protected readonly data = signal<InsightsData | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  protected readonly scopeNote = computed(() => {
    const country = this.country();
    const currency = this.data()?.summary.currency;
    return country
      ? `Showing ${country} only, in ${currency ?? 'local currency'}.`
      : 'Company-wide figures are converted to USD at fixed rates. The country tab shows each country in its own currency.';
  });

  private readonly largestBand = computed(() =>
    Math.max(1, ...(this.data()?.distribution.bands.map((b) => b.headcount) ?? [])),
  );

  constructor() {
    // switchMap cancels the in-flight requests if the HR manager changes the country again quickly.
    this.country$
      .pipe(
        tap(() => {
          this.loading.set(true);
          this.error.set(null);
        }),
        switchMap((country) =>
          forkJoin({
            summary: this.insights.summary(country),
            distribution: this.insights.distribution(country),
            byDepartment: this.insights.byDepartment(country),
            byJobTitle: this.insights.byJobTitle(country),
            byCountry: this.insights.byCountry(),
          }).pipe(
            catchError(() => {
              this.loading.set(false);
              this.error.set('Could not load insights. Please check the connection and try again.');
              return EMPTY;
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((data) => {
        this.data.set(data);
        this.loading.set(false);
      });
  }

  ngOnInit(): void {
    this.employees.countries().subscribe({ next: (countries) => this.countries.set(countries) });
  }

  protected selectCountry(country: string): void {
    this.country.set(country);
    this.country$.next(country);
  }

  protected bandWidth(headcount: number): number {
    return (headcount / this.largestBand()) * 100;
  }

  protected share(headcount: number, total: number): string {
    return total > 0 ? `${((headcount / total) * 100).toFixed(1)}%` : '0%';
  }
}
