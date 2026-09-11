import { Authenticator } from '@aws-amplify/ui-react'
import { AuthGateProvider } from './lib/authGate'
import { AuthModal } from './components/AuthModal'
import { MapScreen } from './components/MapScreen'
import './App.css'

function App() {
  return (
    <Authenticator.Provider>
      <AuthGateProvider>
        <MapScreen />
        <AuthModal />
      </AuthGateProvider>
    </Authenticator.Provider>
  )
}

export default App
