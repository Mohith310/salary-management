import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CountryCurrency,
  Employee,
  EmployeeQuery,
  EmployeeRequest,
  FilterOptions,
  PageResponse,
} from './models';

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/employees';

  list(query: EmployeeQuery): Observable<PageResponse<Employee>> {
    let params = new HttpParams()
      .set('page', query.page)
      .set('size', query.size)
      .set('sortBy', query.sortBy)
      .set('direction', query.direction);

    // Only send filters that have a value, so the URL stays readable and the server sees "no filter".
    for (const [key, value] of Object.entries(query.filter)) {
      if (value !== undefined && value !== null && String(value).trim() !== '') {
        params = params.set(key, String(value).trim());
      }
    }
    return this.http.get<PageResponse<Employee>>(this.base, { params });
  }

  get(id: number): Observable<Employee> {
    return this.http.get<Employee>(`${this.base}/${id}`);
  }

  create(request: EmployeeRequest): Observable<Employee> {
    return this.http.post<Employee>(this.base, request);
  }

  update(id: number, request: EmployeeRequest): Observable<Employee> {
    return this.http.put<Employee>(`${this.base}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  filterOptions(): Observable<FilterOptions> {
    return this.http.get<FilterOptions>(`${this.base}/filter-options`);
  }

  countries(): Observable<CountryCurrency[]> {
    return this.http.get<CountryCurrency[]>('/api/countries');
  }
}
