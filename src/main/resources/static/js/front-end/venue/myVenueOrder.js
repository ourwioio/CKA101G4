function openReviewModal(btn) {
    var orderId = btn.getAttribute('data-order-id');
    var rating = btn.getAttribute('data-rating');
    var comment = btn.getAttribute('data-comment') || '';

    document.getElementById('reviewOrderId').value = orderId;
    document.getElementById('reviewComment').value = (comment === 'null') ? '' : comment;
    updateReviewCommentCounter();

    document.querySelectorAll('input[name="venueRating"]').forEach(function (el) {
        el.checked = false;
    });

    if (rating && rating !== 'null') {
        var starEl = document.getElementById('star' + rating);
        if (starEl) starEl.checked = true;
    }

    document.getElementById('reviewModalOverlay').style.display = 'flex';
}

function updateReviewCommentCounter() {
    var textarea = document.getElementById('reviewComment');
    var max = parseInt(textarea.getAttribute('maxlength'), 10);
    var remaining = max - textarea.value.length;
    document.getElementById('reviewCommentCounter').textContent = '還可以輸入 ' + remaining + ' 字';
}

function closeReviewModal() {
    document.getElementById('reviewModalOverlay').style.display = 'none';
}

// 評價留言真的擋住不能超過 3 行（不只是視覺上限）
var REVIEW_COMMENT_MAX_LINES = 3;

document.getElementById('reviewComment').addEventListener('keydown', function (e) {
    if (e.key === 'Enter') {
        var lineCount = this.value.split('\n').length;
        if (lineCount >= REVIEW_COMMENT_MAX_LINES) {
            e.preventDefault();
        }
    }
});

document.getElementById('reviewComment').addEventListener('paste', function (e) {
    var textarea = this;
    var currentLines = textarea.value.split('\n').length;
    var remainingLines = REVIEW_COMMENT_MAX_LINES - currentLines;

    e.preventDefault();
    if (remainingLines <= 0) return; // 已經滿 3 行，貼上的內容整個不接受

    var pasteText = (e.clipboardData || window.clipboardData).getData('text');
    var allowedText = pasteText.split('\n').slice(0, remainingLines + 1).join('\n');

    var start = textarea.selectionStart;
    var end = textarea.selectionEnd;
    var newValue = textarea.value.slice(0, start) + allowedText + textarea.value.slice(end);
    textarea.value = newValue.slice(0, textarea.maxLength);
    updateReviewCommentCounter();
});

document.getElementById('reviewModalOverlay').addEventListener('click', function (e) {
    if (e.target === this) closeReviewModal();
});

function openViewReviewModal(btn) {
    var rating = parseInt(btn.getAttribute('data-rating'), 10) || 0;
    var comment = btn.getAttribute('data-comment');

    var starsHtml = '';
    for (var i = 1; i <= 5; i++) {
        starsHtml += '<span class="' + (i <= rating ? 'filled' : '') + '">★</span>';
    }
    document.getElementById('viewReviewStars').innerHTML = starsHtml;

    var commentEl = document.getElementById('viewReviewComment');
    if (comment && comment !== 'null' && comment.trim() !== '') {
        commentEl.textContent = comment;
        commentEl.classList.remove('view-review-empty');
    } else {
        commentEl.textContent = '（沒有留下文字評論）';
        commentEl.classList.add('view-review-empty');
    }

    document.getElementById('viewReviewModalOverlay').style.display = 'flex';
}

function closeViewReviewModal() {
    document.getElementById('viewReviewModalOverlay').style.display = 'none';
}

document.getElementById('viewReviewModalOverlay').addEventListener('click', function (e) {
    if (e.target === this) closeViewReviewModal();
});

var REPORT_STATUS_LABELS = { '0': '審核中', '1': '審核通過（檢舉成立）', '2': '審核未通過' };

function openViewReportModal(btn) {
    var content = btn.getAttribute('data-content');
    var time = btn.getAttribute('data-time');
    var status = btn.getAttribute('data-status');

    var contentEl = document.getElementById('viewReportContent');
    contentEl.textContent = (content && content.trim() !== '') ? content : '（沒有留下檢舉內容）';

    document.getElementById('viewReportTime').textContent = time ? '檢舉時間：' + time : '';
    document.getElementById('viewReportStatus').textContent = REPORT_STATUS_LABELS[status] || '審核中';

    document.getElementById('viewReportModalOverlay').style.display = 'flex';
}

function closeViewReportModal() {
    document.getElementById('viewReportModalOverlay').style.display = 'none';
}

document.getElementById('viewReportModalOverlay').addEventListener('click', function (e) {
    if (e.target === this) closeViewReportModal();
});

// 🌟 分頁設定
const PAGE_SIZE = 3;
let currentPage = 1;
let allCards = [];
let filteredCards = [];

function initPagination() {
    const container = document.getElementById('orderListContainer');
    if (!container) return;

    allCards = Array.from(container.querySelectorAll('.order-card'));

    applyFilterAndSort(); // 第一次載入也套用一次（預設值）

    document.getElementById('venueTypeFilter')
        .addEventListener('change', applyFilterAndSort);
    document.getElementById('sortDirFilter')
        .addEventListener('change', applyFilterAndSort);
}

function applyFilterAndSort() {
    const type = document.getElementById('venueTypeFilter').value;
    const dir = document.getElementById('sortDirFilter').value;

    filteredCards = allCards.filter(card =>
        !type || card.dataset.venueType === type
    );

    filteredCards.sort((a, b) => {
        const dateA = a.dataset.bookDate;
        const dateB = b.dataset.bookDate;
        return dir === 'asc'
            ? dateA.localeCompare(dateB)
            : dateB.localeCompare(dateA);
    });

    // 🌟 關鍵：排序陣列只是改變參考順序，不會改變畫面顯示順序，
    // 一定要用 appendChild 把節點實際搬到新的 DOM 順序，畫面才會跟著變。
    const container = document.getElementById('orderListContainer');
    filteredCards.forEach(card => container.appendChild(card));

    // 先把所有卡片藏起來，再依分頁邏輯顯示 filteredCards 的那一頁
    allCards.forEach(card => card.style.display = 'none');
    renderPage(1);
}

function renderPage(page) {
    currentPage = page;
    const totalPages = Math.ceil(filteredCards.length / PAGE_SIZE) || 1;

    allCards.forEach(card => card.style.display = 'none');

    const start = (page - 1) * PAGE_SIZE;
    const end = start + PAGE_SIZE;
    filteredCards.slice(start, end).forEach(card => card.style.display = '');

    renderPaginationButtons(totalPages, page);

    document.getElementById('orderListContainer')
        .scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function renderPaginationButtons(totalPages, page) {
    const pagination = document.getElementById('pagination');
    pagination.innerHTML = '';

    if (totalPages <= 1) return; // 只有一頁就不顯示分頁

    // 🌟 回到第一頁（不在第一頁時才顯示）
    if (page !== 1) {
        const firstBtn = document.createElement('button');
        firstBtn.textContent = '« 第一頁';
        firstBtn.className = 'page-btn';
        firstBtn.onclick = () => renderPage(1);
        pagination.appendChild(firstBtn);
    }

    // 上一頁
    const prevBtn = document.createElement('button');
    prevBtn.textContent = '‹ 上一頁';
    prevBtn.className = 'page-btn';
    prevBtn.disabled = page === 1;
    prevBtn.onclick = () => renderPage(page - 1);
    pagination.appendChild(prevBtn);

    // 頁碼
    for (let i = 1; i <= totalPages; i++) {
        const btn = document.createElement('button');
        btn.textContent = i;
        btn.className = 'page-btn' + (i === page ? ' active' : '');
        btn.onclick = () => renderPage(i);
        pagination.appendChild(btn);
    }

    // 下一頁
    const nextBtn = document.createElement('button');
    nextBtn.textContent = '下一頁 ›';
    nextBtn.className = 'page-btn';
    nextBtn.disabled = page === totalPages;
    nextBtn.onclick = () => renderPage(page + 1);
    pagination.appendChild(nextBtn);

    // 🌟 到最後一頁（不在最後一頁時才顯示）
    if (page !== totalPages) {
        const lastBtn = document.createElement('button');
        lastBtn.textContent = '最後一頁 »';
        lastBtn.className = 'page-btn';
        lastBtn.onclick = () => renderPage(totalPages);
        pagination.appendChild(lastBtn);
    }
}

function openCancelModal(btn) {
    var orderId = btn.getAttribute('data-order-id');
    document.getElementById('cancelOrderId').value = orderId;
    document.getElementById('cancelReason').value = '';
    document.getElementById('cancelModalOverlay').style.display = 'flex';
}

function closeCancelModal() {
    document.getElementById('cancelModalOverlay').style.display = 'none';
}

document.getElementById('cancelModalOverlay').addEventListener('click', function (e) {
    if (e.target === this) closeCancelModal();
});

document.getElementById('cancelForm').addEventListener('submit', function (e) {
    var reason = document.getElementById('cancelReason').value.trim();

    if (!reason) {
        e.preventDefault();
        alert('請輸入取消原因');
        return;
    }

    if (!confirm('確定要取消這筆訂單嗎？取消後將無法復原。')) {
        e.preventDefault();
    }
});

document.addEventListener('DOMContentLoaded', initPagination);