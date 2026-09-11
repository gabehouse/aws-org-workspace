const ADJECTIVES = [
  'Spicy',
  'Rowdy',
  'Sneaky',
  'Blazing',
  'Crafty',
  'Feisty',
  'Slick',
  'Rapid',
  'Grumpy',
  'Mighty',
  'Dusty',
  'Wobbly',
  'Fearless',
  'Cheeky',
  'Thunderous',
  'Nimble',
  'Scrappy',
  'Golden',
  'Midnight',
  'Suburban',
]

const NOUNS = [
  'Backhand',
  'Forehand',
  'Volley',
  'Lob',
  'DropShot',
  'Slice',
  'Ace',
  'Tiebreak',
  'Baseline',
  'Smash',
  'Rally',
  'LetCord',
  'Topspin',
  'Deuce',
  'Overhead',
  'Serve',
  'Passing',
  'Moonball',
]

export function generateHandle(): string {
  const adj = ADJECTIVES[Math.floor(Math.random() * ADJECTIVES.length)]
  const noun = NOUNS[Math.floor(Math.random() * NOUNS.length)]
  const num = 10 + Math.floor(Math.random() * 90)
  return `${adj}${noun}${num}`
}

export const HANDLE_PATTERN = /^[A-Za-z0-9_]{3,24}$/

export function validateHandle(handle: string): string | null {
  if (!HANDLE_PATTERN.test(handle)) {
    return 'Handles are 3–24 characters: letters, numbers, underscores'
  }
  return null
}
