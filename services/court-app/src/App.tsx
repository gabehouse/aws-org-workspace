import { Authenticator } from '@aws-amplify/ui-react'
import { MapScreen } from './components/MapScreen'
import './App.css'

function App() {
  return (
    <Authenticator>
      <MapScreen />
    </Authenticator>
  )
}

export default App
