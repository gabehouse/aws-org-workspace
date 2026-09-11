import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { Amplify } from 'aws-amplify'
import outputs from '../amplify_outputs.json'
import '@aws-amplify/ui-react/styles.css'
import { initTheme } from './lib/themePrefs'
import './index.css'
import App from './App.tsx'

initTheme()

// Match relaxed Cognito policy in amplify/backend.ts (min length only).
const amplifyConfig = {
  ...outputs,
  auth: {
    ...outputs.auth,
    password_policy: {
      min_length: 8,
      require_lowercase: false,
      require_numbers: false,
      require_symbols: false,
      require_uppercase: false,
    },
  },
}

Amplify.configure(amplifyConfig)

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
