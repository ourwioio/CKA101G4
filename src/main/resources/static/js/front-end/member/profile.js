const ITEMS_PER_PAGE = 4;
const pageState = { service: 1, activity: 1, venue: 1 };

function setupPagination(type) {
  const grid = document.getElementById(type + "Grid");
  const cards = grid.querySelectorAll(".item-card");
  const totalPages = Math.max(1, Math.ceil(cards.length / ITEMS_PER_PAGE));

  function render() {
    const page = pageState[type];
    cards.forEach((card, idx) => {
      const start = (page - 1) * ITEMS_PER_PAGE;
      const end = start + ITEMS_PER_PAGE;
      card.style.display = idx >= start && idx < end ? "flex" : "none";
    });

    const pageInfo = document.getElementById(type + "PageInfo");
    if (pageInfo) pageInfo.textContent = `${page} / ${totalPages}`;

    const prevBtn = document.getElementById(type + "PrevBtn");
    const nextBtn = document.getElementById(type + "NextBtn");
    if (prevBtn) prevBtn.disabled = page <= 1;
    if (nextBtn) nextBtn.disabled = page >= totalPages;
  }

  render();
  return render;
}

const renderFns = {};

window.changePage = function (type, direction) {
  const cards = document.getElementById(type + "Grid").querySelectorAll(".item-card");
  const totalPages = Math.max(1, Math.ceil(cards.length / ITEMS_PER_PAGE));
  const newPage = pageState[type] + direction;

  if (newPage < 1 || newPage > totalPages) return;

  pageState[type] = newPage;
  renderFns[type]();
};

document.addEventListener("DOMContentLoaded", function () {
  renderFns.service = setupPagination("service");
  renderFns.activity = setupPagination("activity");
  renderFns.venue = setupPagination("venue");
});