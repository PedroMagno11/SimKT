const VERSION = 'v0.1.0';
const GITHUB_URL = 'https://github.com/PedroMagno11/SimKT';

// Flat nav definition — section headers are objects, pages are [file, label] arrays.
// This drives the sidebar, active highlighting, and prev/next navigation.
const NAV = [
  { section: 'Começando' },
  ['installation.html',       'Instalação'],
  ['getting-started.html',    'Primeiros Passos'],

  { section: 'Núcleo' },
  ['environment.html',        'Environment'],
  ['simulation.html',         'Simulation'],
  ['sim-time.html',           'SimTime'],
  ['sim-config.html',         'SimConfig'],

  { section: 'Eventos & Processos' },
  ['sim-event.html',          'SimEvent'],
  ['sim-process.html',        'SimProcess'],
  ['sim-process-context.html','SimProcessContext'],

  { section: 'Primitivos' },
  ['sim-resource.html',       'SimResource'],
  ['sim-channel.html',        'SimChannel'],
  ['sim-queue.html',          'SimQueue'],
  ['sim-container.html',      'SimContainer'],
  ['sim-continuous.html',     'SimContinuousModel'],

  { section: 'Utilitários' },
  ['sim-entity.html',         'SimEntity'],
  ['sim-monitor.html',        'SimMonitor'],
  ['sim-random.html',         'SimRandomProvider'],
  ['sim-topic.html',          'SimTopic'],
  ['coordinates.html',        'Coordenadas'],

  { section: 'Exemplos' },
  ['examples.html',              'Visão Geral'],
  ['drone-radar.html',           'Drone e Radar'],
  ['example-basic-sensor.html',  'Sensor Básico'],
  ['example-resource.html',      'Recurso Compartilhado'],
  ['example-channel.html',       'Canal de Comunicação'],
  ['example-producer-consumer.html', 'Produtor-Consumidor'],
  ['example-gas-station.html',   'Posto de Combustível'],
  ['example-mission-control.html','Controle de Missão'],
  ['example-battery.html',       'Bateria e Sensores'],
  ['example-continuous.html',    'Movimento Contínuo'],

  { section: 'Projeto' },
  ['roadmap.html',    'Roadmap'],
  ['faq.html',        'FAQ'],
  ['contributing.html','Contribuindo'],
];

const PAGES = NAV.filter(item => Array.isArray(item));

function getBasePath() {
  return window.location.pathname.includes('/pages/') ? '..' : '.';
}

function renderHeader() {
  const base = getBasePath();
  document.getElementById('header').innerHTML = `
    <header class="site-header">
      <div class="container navbar">
        <a class="brand" href="${base}/index.html">
          <img class="logo-img" src="${base}/assets/logo.png" alt="Logo SimKT">
          <span>SimKT <small>${VERSION} · experimental</small></span>
        </a>
        <nav class="nav-links" id="navLinks" role="navigation" aria-label="Navegação principal">
          <a data-page="index.html"           href="${base}/index.html">Início</a>
          <a data-page="installation.html"    href="${base}/pages/installation.html">Instalação</a>
          <a data-page="getting-started.html" href="${base}/pages/getting-started.html">Docs</a>
          <a data-page="examples.html"        href="${base}/pages/examples.html">Exemplos</a>
          <a href="${GITHUB_URL}" target="_blank" rel="noopener noreferrer" class="github-link" aria-label="Ver no GitHub">
            <svg width="17" height="17" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
              <path d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z"/>
            </svg>
            GitHub
          </a>
        </nav>
        <button class="menu-button" id="menuButton" aria-expanded="false" aria-controls="navLinks">Menu</button>
      </div>
    </header>`;
}

function renderSidebar(activePage = '') {
  const base = getBasePath();
  const sidebar = document.getElementById('sidebar');
  if (!sidebar) return;

  let html = '';
  NAV.forEach(item => {
    if (!Array.isArray(item)) {
      html += `<div class="sidebar-section">${item.section}</div>`;
    } else {
      const [file, label] = item;
      html += `<a data-page="${file}" href="${base}/pages/${file}">${label}</a>`;
    }
  });

  sidebar.innerHTML = html;
}

function renderPrevNext(activePage) {
  const base = getBasePath();
  const idx = PAGES.findIndex(([file]) => file === activePage);
  if (idx === -1) return;

  const prev = PAGES[idx - 1];
  const next = PAGES[idx + 1];
  if (!prev && !next) return;

  const html = `
    <nav class="prev-next" aria-label="Página anterior e próxima">
      ${prev
        ? `<a href="${base}/pages/${prev[0]}" class="prev-next-link">← ${prev[1]}</a>`
        : '<span></span>'}
      ${next
        ? `<a href="${base}/pages/${next[0]}" class="prev-next-link">${next[1]} →</a>`
        : '<span></span>'}
    </nav>`;

  const placeholder = document.getElementById('prev-next');
  if (placeholder) {
    placeholder.outerHTML = html;
  } else {
    const content = document.querySelector('.doc-content');
    if (content) content.insertAdjacentHTML('beforeend', html);
  }
}

function renderFooter() {
  document.getElementById('footer').innerHTML = `
    <footer class="site-footer">
      <div class="container footer-inner">
        <span>SimKT ${VERSION} — Discrete Event Simulation Toolkit for Kotlin</span>
        <span>MIT License · <a href="${GITHUB_URL}" target="_blank" rel="noopener noreferrer">GitHub</a></span>
      </div>
    </footer>`;
}

function injectPrism() {
  const link = document.createElement('link');
  link.rel = 'stylesheet';
  link.href = 'https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/themes/prism-tomorrow.min.css';
  document.head.appendChild(link);

  const core = document.createElement('script');
  core.src = 'https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-core.min.js';
  core.onload = () => {
    const bash = document.createElement('script');
    bash.src = 'https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-bash.min.js';
    document.head.appendChild(bash);

    const xml = document.createElement('script');
    xml.src = 'https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-markup.min.js';
    document.head.appendChild(xml);

    const kt = document.createElement('script');
    kt.src = 'https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-kotlin.min.js';
    kt.onload = () => { if (typeof Prism !== 'undefined') Prism.highlightAll(); };
    document.head.appendChild(kt);
  };
  document.head.appendChild(core);
}

function renderLayout(activePage) {
  renderHeader();
  renderSidebar(activePage);
  renderPrevNext(activePage);
  renderFooter();
  injectPrism();
}
