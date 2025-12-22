# Muimi blog scaffold

A minimalistic, coder-inspired static blog powered by a lightweight `squint-cljs` shim and the `@w3t-ab/sqeave` UI helpers. Posts live in Markdown, render into a dark, monospace layout, and include a sticky left-hand TOC plus a right-hand category browser.

## Features
- Write posts in Markdown with YAML front matter (`title`, `date`, `category`, `tags`, `summary`).
- Auto-generated headings TOC (sticky on the left) and category browser (sticky on the right).
- Categories baked in: science, tech, politics, philosophy, business.
- Simple email subscribe block that opens a mailto draft for new updates.
- Offline-friendly build using the bundled squint compiler shim and local `@w3t-ab/sqeave` package.

## Commands
- Install local packages (no external registry calls):
  ```bash
  npm install
  ```
- Build static assets (compiles CLJS, copies `sqeave`, generates `public/assets/posts.js`):
  ```bash
  npm run build
  ```
- Serve the site locally after building:
  ```bash
  npm run dev
  # opens http://localhost:4173
  ```

## Writing posts
1. Add a Markdown file under `content/posts/` with front matter:
   ```markdown
   ---
   title: "New idea"
   date: "2025-01-01"
   category: "tech"
   tags: ["demo", "notes"]
   summary: "One-liner summary."
   ---
   # Heading
   ## Sub-heading
   Content goes here.
   ```
2. Run `npm run build` to regenerate `public/assets/posts.js` and recompile the CLJS entrypoint.

## Architecture
- `packages/@w3t-ab/sqeave`: DOM + markdown + layout utilities, exposed globally as `window.sqeave` for the browser and as a CommonJS module for the build scripts.
- `packages/squint-cljs`: tiny S-expression compiler that transpiles `src/app.cljs` into `public/assets/app.js`.
- `scripts/build.js`: orchestrates compilation, post ingestion, and asset copying.
- `scripts/dev.js`: static file server that reuses the build pipeline before hosting `public/`.

The resulting static bundle is fully contained in `public/`, ready to host on any static file server.
