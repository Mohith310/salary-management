export interface Employee {
  id: number;
  fullName: string;
  email: string;
  jobTitle: string;
  department: string;
  country: string;
  currency: string;
  salary: number;
  hireDate: string; // ISO date, yyyy-MM-dd
}

/** What the form sends. The server derives currency from the country. */
export type EmployeeRequest = Omit<Employee, 'id' | 'currency'>;

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface EmployeeFilter {
  q?: string;
  country?: string;
  department?: string;
  jobTitle?: string;
  minSalary?: number | null;
  maxSalary?: number | null;
}

export type SortDirection = 'asc' | 'desc';

export interface EmployeeQuery {
  filter: EmployeeFilter;
  page: number;
  size: number;
  sortBy: string;
  direction: SortDirection;
}

export interface FilterOptions {
  countries: string[];
  departments: string[];
  jobTitles: string[];
}

export interface CountryCurrency {
  country: string;
  currency: string;
  usdRate: number;
}

export interface SalaryStats {
  headcount: number;
  min: number;
  max: number;
  average: number;
  median: number;
  total: number;
}

export interface GroupInsight {
  group: string;
  currency: string;
  stats: SalaryStats;
}

export interface DistributionBand {
  label: string;
  headcount: number;
}

export interface DistributionResponse {
  currency: string;
  bands: DistributionBand[];
}

/** RFC 7807 body returned by the API on errors. */
export interface ProblemDetail {
  status?: number;
  detail?: string;
  fieldErrors?: Record<string, string>;
}
