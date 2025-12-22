import { defineConfig } from 'vite';
import squint from '@w3t-ab/vite-plugin-squint';
import solid from 'vite-plugin-solid';

export default defineConfig({
  plugins: [squint({ scan: true }), solid()],
  build: {
    outDir: 'dist/',
    target: 'esnext',
  },
});
