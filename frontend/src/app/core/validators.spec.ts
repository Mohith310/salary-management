import { FormControl } from '@angular/forms';
import { describe, expect, it } from 'vitest';
import { matchOptions, notInFuture, todayIso } from './validators';

describe('todayIso', () => {
  it('formats a date as yyyy-MM-dd with zero padding', () => {
    expect(todayIso(new Date(2026, 0, 5))).toBe('2026-01-05');
    expect(todayIso(new Date(2026, 10, 25))).toBe('2026-11-25');
  });
});

describe('notInFuture', () => {
  const validator = notInFuture(() => '2026-10-19');

  it('accepts today and past dates', () => {
    expect(validator(new FormControl('2026-10-19'))).toBeNull();
    expect(validator(new FormControl('2020-02-29'))).toBeNull();
  });

  it('rejects dates after today', () => {
    expect(validator(new FormControl('2026-10-20'))).toEqual({ future: true });
  });

  it('ignores empty values so "required" can report them', () => {
    expect(validator(new FormControl(''))).toBeNull();
    expect(validator(new FormControl(null))).toBeNull();
  });
});

describe('matchOptions', () => {
  const options = ['India', 'Indonesia', 'United States', 'Brazil'];

  it('matches case-insensitively anywhere in the option', () => {
    expect(matchOptions(options, 'IND')).toEqual(['India', 'Indonesia']);
    expect(matchOptions(options, 'states')).toEqual(['United States']);
  });

  it('returns all options (up to the limit) for empty input', () => {
    expect(matchOptions(options, '')).toEqual(options);
    expect(matchOptions(options, null, 2)).toEqual(['India', 'Indonesia']);
  });

  it('returns nothing when no option matches', () => {
    expect(matchOptions(options, 'zzz')).toEqual([]);
  });
});
