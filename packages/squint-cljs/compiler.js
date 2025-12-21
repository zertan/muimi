const fs = require('fs');

class Token {
  constructor(type, value = null) {
    this.type = type;
    this.value = value;
  }
}

function tokenize(code) {
  const tokens = [];
  let current = '';
  let inString = false;
  let escape = false;
  for (let i = 0; i < code.length; i += 1) {
    const ch = code[i];
    if (inString) {
      if (escape) {
        current += ch;
        escape = false;
      } else if (ch === '\\') {
        escape = true;
      } else if (ch === '"') {
        tokens.push(new Token('string', current));
        current = '';
        inString = false;
      } else {
        current += ch;
      }
      continue;
    }

    if (ch === '"') {
      inString = true;
      continue;
    }

    if (ch === ';') {
      while (i < code.length && code[i] !== '\n') i += 1;
      continue;
    }

    if (/\s/.test(ch)) {
      if (current.length) {
        tokens.push(new Token('atom', current));
        current = '';
      }
      continue;
    }

    if (ch === '(') {
      if (current.length) {
        tokens.push(new Token('atom', current));
        current = '';
      }
      tokens.push(new Token('open_list'));
      continue;
    }

    if (ch === ')') {
      if (current.length) {
        tokens.push(new Token('atom', current));
        current = '';
      }
      tokens.push(new Token('close_list'));
      continue;
    }

    if (ch === '[') {
      if (current.length) {
        tokens.push(new Token('atom', current));
        current = '';
      }
      tokens.push(new Token('open_vector'));
      continue;
    }

    if (ch === ']') {
      if (current.length) {
        tokens.push(new Token('atom', current));
        current = '';
      }
      tokens.push(new Token('close_vector'));
      continue;
    }

    if (ch === '{') {
      if (current.length) {
        tokens.push(new Token('atom', current));
        current = '';
      }
      tokens.push(new Token('open_map'));
      continue;
    }

    if (ch === '}') {
      if (current.length) {
        tokens.push(new Token('atom', current));
        current = '';
      }
      tokens.push(new Token('close_map'));
      continue;
    }

    current += ch;
  }

  if (current.length) {
    tokens.push(new Token('atom', current));
  }
  return tokens;
}

function parse(tokens) {
  function readForm(idx) {
    const token = tokens[idx];
    if (!token) throw new Error('Unexpected end of tokens');
    if (token.type === 'string') {
      return [{ type: 'string', value: token.value }, idx + 1];
    }
    if (token.type === 'atom') {
      return [readAtom(token.value), idx + 1];
    }
    if (token.type === 'open_list') {
      const items = [];
      let cursor = idx + 1;
      while (cursor < tokens.length && tokens[cursor].type !== 'close_list') {
        const [form, next] = readForm(cursor);
        items.push(form);
        cursor = next;
      }
      if (tokens[cursor]?.type !== 'close_list') throw new Error('Unbalanced list');
      return [{ type: 'list', value: items }, cursor + 1];
    }
    if (token.type === 'open_vector') {
      const items = [];
      let cursor = idx + 1;
      while (cursor < tokens.length && tokens[cursor].type !== 'close_vector') {
        const [form, next] = readForm(cursor);
        items.push(form);
        cursor = next;
      }
      if (tokens[cursor]?.type !== 'close_vector') throw new Error('Unbalanced vector');
      return [{ type: 'vector', value: items }, cursor + 1];
    }
    if (token.type === 'open_map') {
      const items = [];
      let cursor = idx + 1;
      while (cursor < tokens.length && tokens[cursor].type !== 'close_map') {
        const [k, nextK] = readForm(cursor);
        const [v, nextV] = readForm(nextK);
        items.push([k, v]);
        cursor = nextV;
      }
      if (tokens[cursor]?.type !== 'close_map') throw new Error('Unbalanced map');
      return [{ type: 'map', value: items }, cursor + 1];
    }
    throw new Error(`Unhandled token ${token.type}`);
  }

  const forms = [];
  let idx = 0;
  while (idx < tokens.length) {
    const [form, next] = readForm(idx);
    forms.push(form);
    idx = next;
  }
  return forms;
}

function readAtom(value) {
  if (value === 'nil') return { type: 'nil' };
  if (value === 'true' || value === 'false') return { type: 'boolean', value: value === 'true' };
  if (!Number.isNaN(Number(value))) return { type: 'number', value: Number(value) };
  if (value.startsWith(':')) return { type: 'keyword', value: value.slice(1) };
  return { type: 'symbol', value };
}

function symbolToJS(name) {
  if (name.includes('/')) {
    const [ns, sym] = name.split('/');
    if (ns === 'js') {
      return sanitize(sym);
    }
    return `${ns}.${sanitize(sym)}`;
  }
  return sanitize(name);
}

function sanitize(name) {
  return name.replace(/-/g, '_').replace(/!/g, '!');
}

function compile(forms) {
  return forms.map((form) => compileNode(form, { topLevel: true })).join('\n');
}

function compileNode(node, ctx = { topLevel: false }) {
  switch (node.type) {
    case 'nil':
      return 'null';
    case 'boolean':
      return node.value ? 'true' : 'false';
    case 'number':
      return String(node.value);
    case 'string':
      return JSON.stringify(node.value);
    case 'keyword':
      return JSON.stringify(node.value);
    case 'symbol':
      return symbolToJS(node.value);
    case 'vector':
      return `[${node.value.map((n) => compileNode(n)).join(', ')}]`;
    case 'map':
      return `{${node.value.map(([k, v]) => `${compileKey(k)}: ${compileNode(v)}`).join(', ')}}`;
    case 'list':
      return compileList(node.value, ctx);
    default:
      throw new Error(`Unknown node type ${node.type}`);
  }
}

function compileKey(node) {
  if (node.type === 'keyword' || node.type === 'string') return JSON.stringify(node.value);
  if (node.type === 'symbol') return JSON.stringify(sanitize(node.value));
  throw new Error('Invalid map key');
}

function compileList(items, ctx) {
  if (items.length === 0) return 'undefined';
  const head = items[0];
  if (head.type === 'symbol') {
    switch (head.value) {
      case 'ns':
        return '';
      case 'def':
        return compileDef(items.slice(1));
      case 'defn':
        return compileDefn(items.slice(1));
      case 'let':
        return compileLet(items.slice(1));
      case 'if':
        return compileIf(items.slice(1));
      case 'when':
        return compileWhen(items.slice(1));
      case 'fn':
        return compileFn(items.slice(1));
      case 'assoc':
        return compileAssoc(items.slice(1));
      case 'do':
        return compileDo(items.slice(1));
      default:
        return compileCall(items);
    }
  }
  if (head.type === 'keyword') {
    const target = items[1];
    return `${compileNode(target)}[${compileKey(head)}]`;
  }
  return compileCall(items);
}

function compileDef(parts) {
  const [sym, expr] = parts;
  return `const ${symbolToJS(sym.value)} = ${compileNode(expr)};`;
}

function compileParams(vec) {
  if (vec.type !== 'vector') throw new Error('Function params must be a vector');
  return vec.value.map((n) => symbolToJS(n.value)).join(', ');
}

function compileBody(body) {
  if (body.length === 0) return 'return null;';
  if (body.length === 1) return `return ${compileNode(body[0])};`;
  const head = body.slice(0, -1).map((expr) => `${compileNode(expr)};`).join('\n');
  const tail = `return ${compileNode(body[body.length - 1])};`;
  return `${head}\n${tail}`;
}

function compileDefn(parts) {
  const [sym, params, ...body] = parts;
  return `function ${symbolToJS(sym.value)}(${compileParams(params)}) {\n${compileBody(body)}\n}`;
}

function compileFn(parts) {
  const [params, ...body] = parts;
  return `(${compileParams(params)}) => {\n${compileBody(body)}\n}`;
}

function compileLet(parts) {
  const [bindings, ...body] = parts;
  if (bindings.type !== 'vector') throw new Error('let bindings must be vector');
  const pairs = [];
  for (let i = 0; i < bindings.value.length; i += 2) {
    const sym = bindings.value[i];
    const val = bindings.value[i + 1];
    pairs.push({ sym, val });
  }
  const decls = pairs.map(({ sym, val }) => `const ${symbolToJS(sym.value)} = ${compileNode(val)};`).join('\n');
  return `(() => {\n${decls}\n${compileBody(body)}\n})()`;
}

function compileIf(parts) {
  const [test, thenForm, elseForm] = parts;
  const elseCode = typeof elseForm === 'undefined' ? 'null' : compileNode(elseForm);
  return `(${compileNode(test)} ? ${compileNode(thenForm)} : ${elseCode})`;
}

function compileWhen(parts) {
  const [test, ...body] = parts;
  return `(${compileNode(test)} ? (() => {\n${compileBody(body)}\n})() : null)`;
}

function compileAssoc(parts) {
  const [target, ...rest] = parts;
  const base = compileNode(target);
  const kvPairs = [];
  for (let i = 0; i < rest.length; i += 2) {
    kvPairs.push({ k: rest[i], v: rest[i + 1] });
  }
  const mutations = kvPairs.map(({ k, v }) => `next[${compileKey(k)}] = ${compileNode(v)};`).join('\n');
  return `(() => {\nconst next = Object.assign({}, ${base});\n${mutations}\nreturn next;\n})()`;
}

function compileDo(forms) {
  if (forms.length === 0) return 'null';
  if (forms.length === 1) return compileNode(forms[0]);
  const head = forms.slice(0, -1).map((f) => `${compileNode(f)};`).join('\n');
  const tail = compileNode(forms[forms.length - 1]);
  return `(() => {\n${head}\nreturn ${tail};\n})()`;
}

function compileCall(items) {
  const callee = compileNode(items[0]);
  const args = items.slice(1).map((a) => compileNode(a)).join(', ');
  return `${callee}(${args})`;
}

function compileFile(path) {
  const source = fs.readFileSync(path, 'utf8');
  const tokens = tokenize(source);
  const forms = parse(tokens);
  return compile(forms);
}

module.exports = { tokenize, parse, compile, compileFile };
