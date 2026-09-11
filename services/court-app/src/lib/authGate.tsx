import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from 'react'
import { useAuthenticator } from '@aws-amplify/ui-react'

type AuthIntent = 'signIn' | 'signUp'

type AuthGateValue = {
  signedIn: boolean
  userId: string | undefined
  /** Open the auth modal; optionally run `action` after a successful sign-in. */
  requireAuth: (action?: () => void) => boolean
  openAuth: (intent?: AuthIntent) => void
  closeAuth: () => void
  authOpen: boolean
  authIntent: AuthIntent
}

const AuthGateContext = createContext<AuthGateValue | null>(null)

export function AuthGateProvider({ children }: { children: ReactNode }) {
  const { authStatus, user } = useAuthenticator((ctx) => [ctx.authStatus, ctx.user])
  const signedIn = authStatus === 'authenticated'
  const userId = user?.userId

  const [authOpen, setAuthOpen] = useState(false)
  const [authIntent, setAuthIntent] = useState<AuthIntent>('signIn')
  const pendingActionRef = useRef<(() => void) | null>(null)

  const closeAuth = useCallback(() => {
    setAuthOpen(false)
    pendingActionRef.current = null
  }, [])

  const openAuth = useCallback((intent: AuthIntent = 'signIn') => {
    setAuthIntent(intent)
    setAuthOpen(true)
  }, [])

  const requireAuth = useCallback(
    (action?: () => void) => {
      if (signedIn) {
        action?.()
        return true
      }
      pendingActionRef.current = action ?? null
      setAuthIntent('signIn')
      setAuthOpen(true)
      return false
    },
    [signedIn],
  )

  useEffect(() => {
    if (!signedIn || !authOpen) return
    const pending = pendingActionRef.current
    pendingActionRef.current = null
    setAuthOpen(false)
    pending?.()
  }, [signedIn, authOpen])

  const value = useMemo(
    () => ({
      signedIn,
      userId,
      requireAuth,
      openAuth,
      closeAuth,
      authOpen,
      authIntent,
    }),
    [signedIn, userId, requireAuth, openAuth, closeAuth, authOpen, authIntent],
  )

  return <AuthGateContext.Provider value={value}>{children}</AuthGateContext.Provider>
}

export function useAuthGate(): AuthGateValue {
  const ctx = useContext(AuthGateContext)
  if (!ctx) throw new Error('useAuthGate must be used within AuthGateProvider')
  return ctx
}
