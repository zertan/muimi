# Muimi + sqeave quickstart

This repo now follows the official sqeave Non-template Quickstart and restores the minimalist blog layout (categories, post list, hero, content) rendered with the real `@w3t-ab/sqeave`. Styling now relies on Tailwind via the CDN, and the subscribe form lives in the left column.

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

The build also emits an RSS feed at `dist/rss.xml` using the post metadata in `src/content/posts.js`. Set `SITE_URL` (or `VITE_SITE_URL`) to the canonical site URL before building so links in the feed resolve correctly. The subscribe form posts to Mailchimp; configure `VITE_MAILCHIMP_FORM_URL` or the trio `VITE_MAILCHIMP_DC`, `VITE_MAILCHIMP_U`, and `VITE_MAILCHIMP_ID` before deploying.

## Files
- `src/main/index.cljs` – Root component wiring sqeave context to Solid.
- `src/main/main.cljs` – Blog UI with categories, post list, hero/content, and the left-column subscribe card.
- `vite.config.js` – Vite + sqeave + Solid config.
- `squint.edn` – squint paths/output.
- `index.html` – Entry document exporting the CLJS module.
- `src/content/posts.js` – Shared post metadata powering the UI and RSS feed.
