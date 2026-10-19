import { ValidatorFn } from '@angular/forms';

/** Today's date in the user's time zone as yyyy-MM-dd. */
export function todayIso(now: Date = new Date()): string {
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${now.getFullYear()}-${month}-${day}`;
}

/** Rejects ISO dates after today. ISO dates sort lexicographically, so a string comparison is enough. */
export function notInFuture(today: () => string = () => todayIso()): ValidatorFn {
  return (control) => {
    const value = control.value as string | null;
    return value && value > today() ? { future: true } : null;
  };
}

/** Case-insensitive "contains" match used by the autocomplete fields. Returns at most `limit` options. */
export function matchOptions(options: string[], text: string | null, limit = 8): string[] {
  const needle = (text ?? '').trim().toLowerCase();
  const matches = needle ? options.filter((o) => o.toLowerCase().includes(needle)) : options;
  return matches.slice(0, limit);
}
