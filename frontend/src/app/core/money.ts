/** Formats an amount in its own currency, without decimals: $1,234,567 or, compact, $1.2M. */
export function formatMoney(amount: number, currency: string, options: { compact?: boolean } = {}): string {
  const format: Intl.NumberFormatOptions = options.compact
    ? { style: 'currency', currency, notation: 'compact', minimumFractionDigits: 0, maximumFractionDigits: 1 }
    : { style: 'currency', currency, maximumFractionDigits: 0 };
  return new Intl.NumberFormat('en-US', format).format(amount);
}
