import { fetchAuthSession } from 'aws-amplify/auth'

/** Matches gabriel.jsh@gmail.com and gabriel.jsh+{tag}@gmail.com (for docs/tests). */
export function isAdminEmail(email: string): boolean {
  return /^gabriel\.jsh(\+[^@]+)?@gmail\.com$/i.test(email.trim())
}

/** True when the signed-in user is in the Cognito Admins group. */
export async function fetchIsAdmin(): Promise<boolean> {
  const session = await fetchAuthSession()
  const groups = session.tokens?.idToken?.payload['cognito:groups']
  return Array.isArray(groups) && groups.includes('Admins')
}
