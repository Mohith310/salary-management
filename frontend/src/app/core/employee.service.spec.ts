import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { EmployeeService } from './employee.service';
import { EmployeeQuery } from './models';

describe('EmployeeService', () => {
  let service: EmployeeService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(EmployeeService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  const query = (filter: EmployeeQuery['filter']): EmployeeQuery => ({
    filter,
    page: 2,
    size: 25,
    sortBy: 'salary',
    direction: 'desc',
  });

  it('sends paging and sorting parameters', () => {
    service.list(query({})).subscribe();
    const req = http.expectOne((r) => r.url === '/api/employees');
    expect(req.request.params.get('page')).toBe('2');
    expect(req.request.params.get('size')).toBe('25');
    expect(req.request.params.get('sortBy')).toBe('salary');
    expect(req.request.params.get('direction')).toBe('desc');
    req.flush({ content: [], page: 2, size: 25, totalElements: 0, totalPages: 0 });
  });

  it('only sends filters that have a value', () => {
    service.list(query({ q: ' asha ', country: 'India', department: '', jobTitle: undefined, minSalary: null, maxSalary: 90000 })).subscribe();
    const req = http.expectOne((r) => r.url === '/api/employees');
    expect(req.request.params.get('q')).toBe('asha');
    expect(req.request.params.get('country')).toBe('India');
    expect(req.request.params.get('maxSalary')).toBe('90000');
    expect(req.request.params.has('department')).toBe(false);
    expect(req.request.params.has('jobTitle')).toBe(false);
    expect(req.request.params.has('minSalary')).toBe(false);
    req.flush({ content: [], page: 2, size: 25, totalElements: 0, totalPages: 0 });
  });

  it('creates, updates and deletes against the REST resource', () => {
    const body = {
      fullName: 'Asha Rao',
      email: 'asha@acme.com',
      jobTitle: 'Engineer',
      department: 'Engineering',
      country: 'India',
      salary: 1_800_000,
      hireDate: '2024-03-01',
    };

    service.create(body).subscribe();
    const post = http.expectOne('/api/employees');
    expect(post.request.method).toBe('POST');
    expect(post.request.body).toEqual(body);
    post.flush({});

    service.update(7, body).subscribe();
    const put = http.expectOne('/api/employees/7');
    expect(put.request.method).toBe('PUT');
    put.flush({});

    service.delete(7).subscribe();
    const del = http.expectOne('/api/employees/7');
    expect(del.request.method).toBe('DELETE');
    del.flush(null);
  });
});
