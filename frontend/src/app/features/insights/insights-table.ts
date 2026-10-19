import { DecimalPipe } from '@angular/common';
import { Component, computed, input, linkedSignal } from '@angular/core';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { formatMoney } from '../../core/money';
import { GroupInsight, SortDirection } from '../../core/models';

export type InsightColumn = 'group' | 'headcount' | 'min' | 'median' | 'average' | 'max';

/** Returns a sorted copy. Text columns sort alphabetically, numeric columns by value. */
export function sortRows(rows: GroupInsight[], column: InsightColumn, direction: SortDirection): GroupInsight[] {
  const factor = direction === 'asc' ? 1 : -1;
  return [...rows].sort((a, b) => {
    if (column === 'group') {
      return a.group.localeCompare(b.group) * factor;
    }
    return (a.stats[column] - b.stats[column]) * factor;
  });
}

@Component({
  selector: 'app-insights-table',
  imports: [DecimalPipe, MatTableModule, MatSortModule],
  templateUrl: './insights-table.html',
  styleUrl: './insights-table.scss',
})
export class InsightsTable {
  readonly rows = input.required<GroupInsight[]>();
  readonly groupLabel = input.required<string>();
  /** Bars compare averages, so they are only shown when every row is in the same currency. */
  readonly showBars = input(true);
  readonly initialSort = input<{ active: InsightColumn; direction: SortDirection }>({
    active: 'average',
    direction: 'desc',
  });

  protected readonly columns: InsightColumn[] = ['group', 'headcount', 'min', 'median', 'average', 'max'];
  protected readonly formatMoney = formatMoney;

  protected readonly active = linkedSignal<InsightColumn>(() => this.initialSort().active);
  protected readonly direction = linkedSignal<SortDirection>(() => this.initialSort().direction);
  protected readonly sorted = computed(() => sortRows(this.rows(), this.active(), this.direction()));
  private readonly maxAverage = computed(() => Math.max(0, ...this.rows().map((r) => r.stats.average)));

  protected onSort(sort: Sort): void {
    this.active.set(sort.active as InsightColumn);
    this.direction.set(sort.direction === 'desc' ? 'desc' : 'asc');
  }

  protected barWidth(average: number): number {
    const max = this.maxAverage();
    return max > 0 ? (average / max) * 100 : 0;
  }
}
