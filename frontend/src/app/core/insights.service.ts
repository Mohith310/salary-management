import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { DistributionResponse, GroupInsight } from './models';

@Injectable({ providedIn: 'root' })
export class InsightsService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/insights';

  summary(country?: string): Observable<GroupInsight> {
    return this.http.get<GroupInsight>(`${this.base}/summary`, { params: countryParams(country) });
  }

  byCountry(): Observable<GroupInsight[]> {
    return this.http.get<GroupInsight[]>(`${this.base}/by-country`);
  }

  byDepartment(country?: string): Observable<GroupInsight[]> {
    return this.http.get<GroupInsight[]>(`${this.base}/by-department`, { params: countryParams(country) });
  }

  byJobTitle(country?: string): Observable<GroupInsight[]> {
    return this.http.get<GroupInsight[]>(`${this.base}/by-job-title`, { params: countryParams(country) });
  }

  distribution(country?: string): Observable<DistributionResponse> {
    return this.http.get<DistributionResponse>(`${this.base}/distribution`, { params: countryParams(country) });
  }
}

function countryParams(country?: string): HttpParams {
  return country ? new HttpParams().set('country', country) : new HttpParams();
}
