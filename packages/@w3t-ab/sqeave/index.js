const accent = '#7dd3fc';
const surface = '#0b0f19';

function slugify(text) {
  return String(text || '')
    .toLowerCase()
    .replace(/[^a-z0-9\s-]/g, '')
    .trim()
    .replace(/\s+/g, '-')
    .replace(/-+/g, '-');
}

function text(content) {
  return document.createTextNode(content);
}

function by_id(id) {
  return document.getElementById(id);
}

function create_node(tag, props = {}, children = []) {
  const el = document.createElement(tag);
  if (props.className) {
    el.className = props.className;
  }
  if (props.attrs) {
    Object.entries(props.attrs).forEach(([k, v]) => {
      if (v !== undefined && v !== null) el.setAttribute(k, v);
    });
  }
  if (props.html) {
    el.innerHTML = props.html;
  }
  children.forEach((child) => {
    if (!child) return;
    el.appendChild(typeof child === 'string' ? text(child) : child);
  });
  return el;
}

function clear(node) {
  while (node.firstChild) node.removeChild(node.firstChild);
}

function set_children(node, children) {
  clear(node);
  children.forEach((child) => node.appendChild(child));
}

function on(node, event, handler) {
  node.addEventListener(event, handler);
  return node;
}

function format_date(input) {
  const date = new Date(input);
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
}

function inline_format(textValue) {
  if (!textValue) return '';
  return textValue
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/_([^_]+)_/g, '<em>$1</em>');
}

function parse_markdown(markdown) {
  const lines = String(markdown || '').split(/\r?\n/);
  const html = [];
  const headings = [];
  let inList = false;
  let inCode = false;
  let codeBuffer = [];
  const seenIds = {};

  function commitList() {
    if (inList) {
      html.push('</ul>');
      inList = false;
    }
  }

  function commitCode() {
    if (inCode) {
      html.push(`<pre><code>${codeBuffer.join('\n')}</code></pre>`);
      codeBuffer = [];
      inCode = false;
    }
  }

  lines.forEach((line) => {
    if (/^```/.test(line.trim())) {
      if (inCode) {
        commitCode();
      } else {
        commitList();
        inCode = true;
      }
      return;
    }

    if (inCode) {
      codeBuffer.push(line.replace(/</g, '&lt;').replace(/>/g, '&gt;'));
      return;
    }

    if (/^\s*-\s+/.test(line)) {
      if (!inList) {
        commitCode();
        html.push('<ul class="md-list">');
        inList = true;
      }
      const item = inline_format(line.replace(/^\s*-\s+/, ''));
      html.push(`<li>${item}</li>`);
      return;
    }

    const headingMatch = /^(#{1,4})\s+(.+)/.exec(line);
    if (headingMatch) {
      commitList();
      const level = headingMatch[1].length;
      const rawText = headingMatch[2].trim();
      const baseId = slugify(rawText);
      const count = (seenIds[baseId] || 0) + 1;
      seenIds[baseId] = count;
      const id = count > 1 ? `${baseId}-${count}` : baseId;
      headings.push({ level, text: rawText, id });
      html.push(`<h${level} id="${id}">${inline_format(rawText)}</h${level}>`);
      return;
    }

    if (line.trim() === '') {
      commitList();
      return;
    }

    commitList();
    const paragraph = inline_format(line.trim());
    if (paragraph.length) html.push(`<p>${paragraph}</p>`);
  });
  commitList();
  commitCode();

  return { html: html.join('\n'), headings };
}

function toc_items(headings) {
  if (!headings.length) {
    return [create_node('div', { className: 'toc-empty' }, [text('No headings yet')])];
  }
  return headings.map((h) => {
    const anchor = create_node('a', {
      className: `toc-link level-${h.level}`,
      attrs: { href: `#${h.id}` },
    }, [text(h.text)]);
    return create_node('div', { className: 'toc-row' }, [anchor]);
  });
}

function render_tags(tags = []) {
  return create_node('div', { className: 'tag-row' }, tags.map((tag) => create_node('span', {
    className: 'tag-pill',
  }, [text(tag)])));
}

function post_card(post, onSelect) {
  const card = create_node('button', { className: 'post-card' }, [
    create_node('div', { className: 'post-card-title' }, [text(post.title)]),
    create_node('div', { className: 'post-card-meta' }, [
      text(`${format_date(post.date)} • ${post.category}`),
    ]),
    create_node('p', { className: 'post-card-summary' }, [text(post.summary || '')]),
    render_tags(post.tags || []),
  ]);
  if (onSelect) on(card, 'click', () => onSelect(post.slug));
  return card;
}

function subscription_block(onSubmit, lastEmail) {
  const input = create_node('input', {
    className: 'subscribe-input',
    attrs: {
      type: 'email',
      placeholder: 'you@example.com',
      value: lastEmail || '',
    },
  });
  const status = create_node('div', { className: 'subscribe-status' }, []);
  const button = create_node('button', { className: 'subscribe-button' }, [text('Notify me')] );
  const form = create_node('div', { className: 'subscribe-block' }, [
    create_node('div', { className: 'subscribe-title' }, [text('Subscribe to updates')]),
    create_node('p', { className: 'subscribe-copy' }, [text('Lightweight email ping when a new post drops.')]),
    create_node('div', { className: 'subscribe-row' }, [input, button]),
    status,
  ]);
  on(button, 'click', () => {
    const email = input.value.trim();
    if (!email) {
      status.textContent = 'Add an email first.';
      return;
    }
    if (onSubmit) onSubmit(email, status);
  });
  return form;
}

function set_main(root, children) {
  set_children(root, children);
}

function build_hero(post) {
  return create_node('header', { className: 'hero' }, [
    create_node('div', { className: 'eyebrow' }, [text(`${post.category.toUpperCase()} • ${format_date(post.date)}`)]),
    create_node('h1', { className: 'post-title' }, [text(post.title)]),
    create_node('p', { className: 'post-summary' }, [text(post.summary || '')]),
    render_tags(post.tags || []),
  ]);
}

function category_menu(categories, activeCategory, onSelect, posts) {
  const buttons = categories.map((cat) => {
    const count = posts.filter((p) => p.category === cat).length;
    const label = `${cat} (${count})`;
    const btn = create_node('button', {
      className: `category-chip ${activeCategory === cat ? 'active' : ''}`,
    }, [text(label)]);
    on(btn, 'click', () => onSelect(activeCategory === cat ? null : cat));
    return btn;
  });
  return create_node('div', { className: 'category-menu' }, [
    create_node('div', { className: 'menu-title' }, [text('Categories')]),
    ...buttons,
  ]);
}

function post_list(posts, activeSlug, onSelect) {
  const nodes = posts.map((p) => {
    const row = create_node('button', {
      className: `category-post ${activeSlug === p.slug ? 'active' : ''}`,
    }, [
      create_node('div', { className: 'category-post-title' }, [text(p.title)]),
      create_node('div', { className: 'category-post-meta' }, [text(format_date(p.date))]),
    ]);
    on(row, 'click', () => onSelect(p.slug));
    return row;
  });
  return create_node('div', { className: 'category-posts' }, [
    create_node('div', { className: 'menu-title subtle' }, [text('Posts')]),
    ...nodes,
  ]);
}

function make_layout() {
  const toc = create_node('aside', { className: 'pane pane-left' }, []);
  const main = create_node('main', { className: 'pane pane-main' }, []);
  const side = create_node('aside', { className: 'pane pane-right' }, []);
  const layout = create_node('div', { className: 'layout' }, [toc, main, side]);
  return { layout, toc, main, side };
}

function start_blog(root, posts, categories) {
  if (!root) throw new Error('App root missing');
  const state = {
    posts: posts || [],
    activeSlug: posts && posts.length ? posts[0].slug : null,
    category: null,
    lastEmail: '',
  };

  const { layout, toc, main, side } = make_layout();
  set_children(root, [layout]);

  function selectPost(slug) {
    state.activeSlug = slug;
    render();
  }

  function selectCategory(cat) {
    state.category = cat;
    if (cat) {
      const filtered = state.posts.filter((p) => p.category === cat);
      if (filtered.length && !filtered.find((p) => p.slug === state.activeSlug)) {
        state.activeSlug = filtered[0].slug;
      }
    }
    render();
  }

  function subscribe(email, statusNode) {
    state.lastEmail = email;
    const subject = encodeURIComponent('Blog subscription');
    const body = encodeURIComponent(`Please add ${email} to the update list.`);
    statusNode.textContent = 'Opening your mail client…';
    window.location.href = `mailto:subscribe@muimi.local?subject=${subject}&body=${body}`;
  }

  function render() {
    const visible = state.category
      ? state.posts.filter((p) => p.category === state.category)
      : state.posts;

    if (!visible.length) {
      set_children(main, [create_node('div', { className: 'empty' }, [text('No posts match this category yet.')])]);
      set_children(toc, [create_node('div', { className: 'toc-empty' }, [text('—')])]);
      return;
    }

    if (!visible.find((p) => p.slug === state.activeSlug)) {
      state.activeSlug = visible[0].slug;
    }
    const current = visible.find((p) => p.slug === state.activeSlug) || visible[0];
    const parsed = parse_markdown(current.content);

    const hero = build_hero(current);
    const article = create_node('article', { className: 'post-body' });
    article.innerHTML = parsed.html;

    const subscribe = subscription_block(subscribeEmail, state.lastEmail);
    const cards = visible.map((p) => post_card(p, selectPost));
    const gallery = create_node('section', { className: 'post-gallery' }, [
      create_node('div', { className: 'menu-title subtle' }, [text('Browse posts')]),
      ...cards,
    ]);

    set_main(main, [hero, gallery, article, subscribe]);
    set_children(toc, toc_items(parsed.headings));

    const menu = category_menu(categories, state.category, selectCategory, state.posts);
    const postsList = post_list(visible, state.activeSlug, selectPost);
    const legend = create_node('div', { className: 'side-legend' }, [
      create_node('div', { className: 'menu-title subtle' }, [text('Recent tags')]),
      render_tags(current.tags || []),
    ]);
    set_children(side, [menu, postsList, legend]);
  }

  function subscribeEmail(email, statusNode) {
    subscribe(email, statusNode);
    statusNode.textContent = `Noted ${email}. Check your mail client.`;
  }

  render();
  return { state, render, selectPost, selectCategory };
}

if (typeof window !== 'undefined') {
  window.sqeave = {
    slugify,
    text,
    by_id,
    create_node,
    set_children,
    parse_markdown,
    toc_items,
    start_blog: start_blog,
    format_date,
  };
}

if (typeof module !== 'undefined') {
  module.exports = {
    slugify,
    text,
    by_id,
    create_node,
    set_children,
    parse_markdown,
    toc_items,
    start_blog,
    format_date,
  };
}
