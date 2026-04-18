import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: '0.0.0.0', // This forces IPv4 instead of the IPv6 loopback
    port: 5174,
    strictPort: true, // Prevents Vite from jumping to 5174 if 5173 is "busy"
    watch: {
      usePolling: true,
      interval: 100, // Check every 100ms
    },
  }

})
