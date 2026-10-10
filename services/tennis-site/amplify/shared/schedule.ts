// Shared by the browser (to render the grid) and the booking Lambda (to validate requests),
// so the server is the authority on which slots can be booked.

export const TIME_ZONE = 'America/Toronto';
export const DAYS_AHEAD = 7;
export const TIME_SLOTS = Array.from({ length: 13 }, (_, i) => `${(6 + i).toString().padStart(2, '0')}:00`);

function zonedParts(now: Date) {
  const parts = new Intl.DateTimeFormat('en-CA', {
    timeZone: TIME_ZONE,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hourCycle: 'h23',
  }).formatToParts(now);
  const get = (type: string) => parts.find((p) => p.type === type)!.value;
  return { date: `${get('year')}-${get('month')}-${get('day')}`, time: `${get('hour')}:${get('minute')}` };
}

/** The next DAYS_AHEAD dates (YYYY-MM-DD) starting today in TIME_ZONE. */
export function getBookableDates(now: Date = new Date()): string[] {
  const today = new Date(`${zonedParts(now).date}T00:00:00Z`);
  return Array.from({ length: DAYS_AHEAD }, (_, i) => {
    const d = new Date(today);
    d.setUTCDate(today.getUTCDate() + i);
    return d.toISOString().slice(0, 10);
  });
}

export function isBookable(dateSlot: string, timeSlot: string, now: Date = new Date()): boolean {
  if (!TIME_SLOTS.includes(timeSlot) || !getBookableDates(now).includes(dateSlot)) {
    return false;
  }
  const { date, time } = zonedParts(now);
  return dateSlot > date || timeSlot > time;
}

export function slotKey(dateSlot: string, timeSlot: string): string {
  return `${dateSlot}#${timeSlot}`;
}
