# Muimi + sqeave quickstart

This repo now follows the official sqeave Non-template Quickstart and restores the minimalist blog layout (categories, post list, hero, content) rendered with the real `@w3t-ab/sqeave`. The email subscribe form is pinned to the lower-left corner.

## Prerequisites
- Node 20+
- pnpm or npm (npm commands shown)

## Install
```bash
npm install
```
> Note: If your environment restricts access to `@w3t-ab/sqeave`, npm will fail with 403. Use a network that can reach the npm registry or add the package to your allowed list.

## Develop
```bash
npm run dev
```
Opens Vite at http://localhost:5173 with hot reload.

## Build
```bash
npm run build
```
Outputs to `dist/`.

## Files
- `src/main/index.cljs` – Root component wiring sqeave context to Solid.
- `src/main/main.cljs` – Blog UI with categories, post list, hero/content, and bottom-left email signup.
- `vite.config.js` – Vite + sqeave + Solid config.
- `squint.edn` – squint paths/output.
- `index.html` – Entry document exporting the CLJS module.
