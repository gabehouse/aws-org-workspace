import { Authenticator } from '@aws-amplify/ui-react'
import { useAuthenticator } from '@aws-amplify/ui-react'
import { useAuthGate } from '../lib/authGate'

/** Modal Cognito sign-in / sign-up; closed automatically after auth succeeds. */
export function AuthModal() {
  const { authOpen, authIntent, closeAuth } = useAuthGate()
  const { authStatus } = useAuthenticator((ctx) => [ctx.authStatus])

  if (!authOpen || authStatus === 'authenticated') return null

  return (
    <div
      className="auth-modal-backdrop"
      role="presentation"
      onClick={closeAuth}
      onKeyDown={(e) => {
        if (e.key === 'Escape') closeAuth()
      }}
    >
      <div
        className="auth-modal"
        role="dialog"
        aria-modal="true"
        aria-label="Sign in"
        onClick={(e) => e.stopPropagation()}
      >
        <button
          type="button"
          className="auth-modal__close btn btn--icon"
          aria-label="Close"
          onClick={closeAuth}
        >
          ×
        </button>
        <p className="auth-modal__lead">Sign in or create an account to continue</p>
        <Authenticator
          key={authIntent}
          initialState={authIntent === 'signUp' ? 'signUp' : 'signIn'}
        />
      </div>
    </div>
  )
}
