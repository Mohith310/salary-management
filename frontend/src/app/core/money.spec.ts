import { describe, expect, it } from 'vitest';
import { formatMoney } from './money';

describe('formatMoney', () => {
  it('formats whole amounts in the given currency without decimals', () => {
    expect(formatMoney(1234567.89, 'USD')).toBe('$1,234,568');
  });

  it('uses the currency symbol of the amount, not a global one', () => {
    expect(formatMoney(5000, 'EUR')).toBe('€5,000');
    expect(formatMoney(5000, 'GBP')).toBe('£5,000');
  });

  it('formats compactly for chart labels', () => {
    expect(formatMoney(1_250_000, 'USD', { compact: true })).toBe('$1.3M');
    expect(formatMoney(85_000, 'USD', { compact: true })).toBe('$85K');
  });
});
