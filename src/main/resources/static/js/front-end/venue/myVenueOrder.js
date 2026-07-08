function openReviewModal(btn) {
    var orderId = btn.getAttribute('data-order-id');
    var rating = btn.getAttribute('data-rating');
    var comment = btn.getAttribute('data-comment') || '';

    document.getElementById('reviewOrderId').value = orderId;
    document.getElementById('reviewComment').value = (comment === 'null') ? '' : comment;

    document.querySelectorAll('input[name="venueRating"]').forEach(function (el) {
        el.checked = false;
    });

    if (rating && rating !== 'null') {
        var starEl = document.getElementById('star' + rating);
        if (starEl) starEl.checked = true;
    }

    document.getElementById('reviewModalOverlay').style.display = 'flex';
}

function closeReviewModal() {
    document.getElementById('reviewModalOverlay').style.display = 'none';
}

document.getElementById('reviewModalOverlay').addEventListener('click', function (e) {
    if (e.target === this) closeReviewModal();
});

// 🌟 分頁設定
const PAGE_SIZE = 3;
let currentPage = 1;

function initPagination() {
    const container = document.getElementById('orderListContainer');
    if (!container) return; // 沒有訂單時不執行

    const cards = Array.from(container.querySelectorAll('.order-card'));
    const totalPages = Math.ceil(cards.length / PAGE_SIZE);

    function renderPage(page) {
        currentPage = page;

        cards.forEach((card, index) => {
            const start = (page - 1) * PAGE_SIZE;
            const end = start + PAGE_SIZE;
            card.style.display = (index >= start && index < end) ? '' : 'none';
        });

        renderPaginationButtons(totalPages, page);

        // 換頁時捲動回列表頂端
        container.scrollIntoView({ behavior: 'smooth', block: 'start' });
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

    renderPage(1);
}

document.addEventListener('DOMContentLoaded', initPagination);