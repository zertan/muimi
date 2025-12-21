const fs = require('fs');
const path = require('path');
const { compileFile } = require('../packages/squint-cljs/compiler');
const sqeave = require('../packages/@w3t-ab/sqeave/index.js');

const projectRoot = path.resolve(__dirname, '..');
const contentDir = path.join(projectRoot, 'content', 'posts');
const publicAssets = path.join(projectRoot, 'public', 'assets');

function ensureDir(target) {
  if (!fs.existsSync(target)) {
    fs.mkdirSync(target, { recursive: true });
  }
}

function parseFrontmatter(raw) {
  const match = /^---\s*\n([\s\S]*?)\n---\s*\n?([\s\S]*)$/m.exec(raw.trim());
  if (!match) return { meta: {}, content: raw.trim() };
  const [, metaRaw, body] = match;
  const meta = {};
  metaRaw.split(/\n/).forEach((line) => {
    const idx = line.indexOf(':');
    if (idx === -1) return;
    const key = line.slice(0, idx).trim();
    const value = line.slice(idx + 1).trim();
    if (!key) return;
    if (value.startsWith('[') && value.endsWith(']')) {
      meta[key] = value
        .slice(1, -1)
        .split(',')
        .map((v) => v.trim().replace(/^"|"$/g, ''))
        .filter(Boolean);
    } else {
      meta[key] = value.replace(/^"|"$/g, '');
    }
  });
  return { meta, content: (body || '').trim() };
}

function loadPosts() {
  const files = fs.readdirSync(contentDir).filter((f) => f.endsWith('.md'));
  const posts = files.map((file) => {
    const raw = fs.readFileSync(path.join(contentDir, file), 'utf8');
    const { meta, content } = parseFrontmatter(raw);
    const title = meta.title || file.replace(/\.md$/, '');
    const slug = sqeave.slugify(meta.slug || title);
    return {
      title,
      slug,
      date: meta.date || new Date().toISOString().slice(0, 10),
      tags: meta.tags || [],
      category: meta.category || 'uncategorized',
      summary: meta.summary || '',
      content,
    };
  });
  return posts.sort((a, b) => new Date(b.date) - new Date(a.date));
}

function writePosts(posts) {
  const target = path.join(publicAssets, 'posts.js');
  const payload = `window.blogPosts = ${JSON.stringify(posts, null, 2)};\n`;
  fs.writeFileSync(target, payload, 'utf8');
}

function copySqeave() {
  const source = path.join(projectRoot, 'packages', '@w3t-ab', 'sqeave', 'index.js');
  const target = path.join(publicAssets, 'sqeave.js');
  const header = '// Auto-copied from packages/sqeave/index.js for browser usage.\n';
  const body = fs.readFileSync(source, 'utf8');
  fs.writeFileSync(target, `${header}${body}`, 'utf8');
}

function compileApp() {
  const src = path.join(projectRoot, 'src', 'app.cljs');
  const target = path.join(publicAssets, 'app.js');
  const compiled = compileFile(src);
  const wrapped = `// Compiled with the offline squint-cljs shim.\n${compiled}\n`;
  fs.writeFileSync(target, wrapped, 'utf8');
}

function main() {
  ensureDir(publicAssets);
  const posts = loadPosts();
  writePosts(posts);
  copySqeave();
  compileApp();
  console.log(`Built ${posts.length} posts.`);
}

if (require.main === module) {
  main();
}

module.exports = { main };
