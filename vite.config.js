import { defineConfig } from 'vite';
import squint from '@w3t-ab/vite-plugin-squint';
import solid from 'vite-plugin-solid';
import fs from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { posts } from './src/content/posts.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const siteUrl = process.env.SITE_URL || process.env.VITE_SITE_URL || 'http://localhost:5173';

function escapeXml(value) {
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&apos;');
}

function rssPlugin() {
  return {
    name: 'muimi-rss-feed',
    apply: 'build',
    async closeBundle() {
      const feedUrl = new URL('/rss.xml', siteUrl).toString();
      const items = posts.map((post) => {
        const url = new URL(`/post/${post.slug}`, siteUrl).toString();
        return [
          '  <item>',
          `    <title>${escapeXml(post.title)}</title>`,
          `    <link>${url}</link>`,
          `    <guid>${url}</guid>`,
          `    <description>${escapeXml(post.summary)}</description>`,
          `    <pubDate>${new Date(post.date).toUTCString()}</pubDate>`,
          `    <category>${escapeXml(post.category)}</category>`,
          '  </item>',
        ].join('\n');
      }).join('\n');

      const xml = [
        '<?xml version="1.0" encoding="UTF-8"?>',
        '<rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom">',
        '<channel>',
        '  <title>Muimi</title>',
        `  <link>${siteUrl}</link>`,
        `  <atom:link href="${feedUrl}" rel="self" type="application/rss+xml" />`,
        '  <description>Latest posts from Muimi</description>',
        '  <language>en-us</language>',
        `  <lastBuildDate>${new Date().toUTCString()}</lastBuildDate>`,
        items,
        '</channel>',
        '</rss>',
      ].join('\n');

      await fs.mkdir(path.resolve(__dirname, 'dist'), { recursive: true });
      await fs.writeFile(path.resolve(__dirname, 'dist/rss.xml'), xml);
    },
  };
}

export default defineConfig({
  plugins: [squint({ scan: true }), solid(), rssPlugin()],
  build: {
    outDir: 'dist/',
    target: 'esnext',
  },
});
