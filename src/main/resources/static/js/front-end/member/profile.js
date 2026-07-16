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

function filterReviews(role, btn) {
  // 切換按鈕的 active 樣式
  document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
  btn.classList.add('active');

  const items = document.querySelectorAll('.review-item');
  items.forEach(item => {
    if (role === 'all' || item.dataset.role === role) {
      item.style.display = '';
    } else {
      item.style.display = 'none';
    }
  });
}

let currentReviewPage = 1;
const reviewsPerPage = 3; // 每頁限制 5 筆評論

function updateReviewsVisibility() {
  const container = document.querySelector('.reviews-container');
  if (!container) return;
  
  const items = Array.from(container.querySelectorAll('.review-item'));
  const activeTab = document.querySelector('.review-tabs button.active, .review-tabs .tab-btn.active');
  const filter = activeTab ? activeTab.getAttribute('data-filter') : 'all';
  
  // 1. 過濾符合當前分類 Role 的評論
  const matchedItems = items.filter(item => {
    const role = item.getAttribute('data-role');
    if (filter === 'all') return true;
    if (filter === 'AS_SELLER') {
      return role === 'AS_SELLER' || role === 'AS_SERVICE_SELLER';
    }
    if (filter === 'AS_BUYER') {
      return role === 'AS_BUYER' || role === 'AS_SERVICE_BUYER';
    }
    return role === filter;
  });
  
  // 2. 計算分頁
  const matchedCount = matchedItems.length;
  const totalPages = Math.max(1, Math.ceil(matchedCount / reviewsPerPage));
  
  if (currentReviewPage > totalPages) currentReviewPage = totalPages;
  if (currentReviewPage < 1) currentReviewPage = 1;
  
  // 3. 隱藏所有，只顯示當前頁面的 5 筆
  items.forEach(item => item.style.display = 'none');
  
  const startIndex = (currentReviewPage - 1) * reviewsPerPage;
  const endIndex = startIndex + reviewsPerPage;
  
  matchedItems.forEach((item, index) => {
    if (index >= startIndex && index < endIndex) {
      item.style.display = 'flex';
    }
  });
  
  // 4. 更新分頁按鈕 DOM 狀態
  const paginationEl = document.getElementById('reviewPagination');
  if (paginationEl) {
    if (totalPages <= 1) {
      paginationEl.style.display = 'none'; // 少於等於 5 筆時，直接優雅隱藏分頁按鈕
    } else {
      paginationEl.style.display = 'flex';
      
      const prevBtn = document.getElementById('reviewPrevBtn');
      const nextBtn = document.getElementById('reviewNextBtn');
      const info = document.getElementById('reviewPageInfo');
      
      if (info) info.textContent = `${currentReviewPage} / ${totalPages}`;
      if (prevBtn) prevBtn.disabled = (currentReviewPage === 1);
      if (nextBtn) nextBtn.disabled = (currentReviewPage === totalPages);
    }
  }
  
  // 5. 特殊防呆：當某個分類沒有任何評論時，顯示無評論小字
  let emptyMessage = document.getElementById('reviewFilterEmpty');
  if (matchedCount === 0) {
    if (!emptyMessage) {
      emptyMessage = document.createElement('div');
      emptyMessage.id = 'reviewFilterEmpty';
      emptyMessage.className = 'no-reviews';
      emptyMessage.innerHTML = '<p>暫無此類別評論</p>';
      container.parentNode.insertBefore(emptyMessage, paginationEl);
    }
    emptyMessage.style.display = 'block';
    container.style.display = 'none';
  } else {
    if (emptyMessage) emptyMessage.style.display = 'none';
    container.style.display = 'flex';
  }
}

// 切換分頁
window.changeReviewPage = function(direction) {
  currentReviewPage += direction;
  updateReviewsVisibility();
};

// 重寫分類點擊事件，以完全支援分頁重置
window.filterReviews = function(filter, btn) {
  const buttons = document.querySelectorAll('.review-tabs button, .review-tabs .tab-btn');
  buttons.forEach(b => b.classList.remove('active'));
  btn.classList.add('active');
  
  currentReviewPage = 1; // 切換 Tab 時強制回第一頁
  updateReviewsVisibility();
};

// DOM 載入完畢後立刻初始化
document.addEventListener('DOMContentLoaded', () => {
  updateReviewsVisibility();
});

