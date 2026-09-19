import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// The browser only talks to our backend; /api is proxied so no keys or CORS in the client.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
