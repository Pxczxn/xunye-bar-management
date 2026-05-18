import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';
import path from 'path';

export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 8847,
    host: '0.0.0.0',
    allowedHosts: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8848',
        changeOrigin: true,
      }
    }
  },
  build: {
    chunkSizeWarningLimit: 900,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) return undefined;
          if (id.includes('react') || id.includes('react-router-dom')) {
            return 'react-vendor';
          }
          if (id.includes('antd') || id.includes('@ant-design')) {
            return 'antd-vendor';
          }
          if (id.includes('echarts')) {
            return 'charts-vendor';
          }
          return 'vendor';
        },
      },
    },
  },
});
