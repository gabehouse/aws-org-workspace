import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: true, // listen on 0.0.0.0 — reachable from phone on same network
    port: 5173,
    allowedHosts: ['.trycloudflare.com'],
  },
})
