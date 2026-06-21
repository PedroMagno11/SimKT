document.addEventListener('DOMContentLoaded', () => {
  // Mobile menu toggle with aria-expanded
  const menuButton = document.getElementById('menuButton');
  const navLinks   = document.getElementById('navLinks');
  if (menuButton && navLinks) {
    menuButton.addEventListener('click', () => {
      const isOpen = navLinks.classList.toggle('open');
      menuButton.setAttribute('aria-expanded', String(isOpen));
    });
  }

  // Mark active links (header nav + sidebar)
  const currentPage = window.location.pathname.split('/').pop() || 'index.html';
  document.querySelectorAll('[data-page]').forEach(link => {
    if (link.getAttribute('data-page') === currentPage) {
      link.classList.add('active');
      link.ariaCurrent = 'page';
    }
  });
});
