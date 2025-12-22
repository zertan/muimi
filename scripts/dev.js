const http = require('http');
const path = require('path');
const fs = require('fs');
const { main: build } = require('./build');

const port = process.env.PORT || 4173;
const publicDir = path.join(__dirname, '..', 'public');

build();

const server = http.createServer((req, res) => {
  const urlPath = req.url.split('?')[0];
  const safePath = urlPath === '/' ? '/index.html' : urlPath;
  const filePath = path.join(publicDir, decodeURIComponent(safePath));
  if (!filePath.startsWith(publicDir)) {
    res.writeHead(403);
    res.end('Forbidden');
    return;
  }
  fs.readFile(filePath, (err, data) => {
    if (err) {
      res.writeHead(404);
      res.end('Not found');
      return;
    }
    const ext = path.extname(filePath);
    const type = {
      '.html': 'text/html',
      '.js': 'application/javascript',
      '.css': 'text/css',
      '.json': 'application/json',
    }[ext] || 'text/plain';
    res.writeHead(200, { 'Content-Type': type });
    res.end(data);
  });
});

server.listen(port, '0.0.0.0', () => {
  console.log(`Dev server on http://localhost:${port}`);
});
