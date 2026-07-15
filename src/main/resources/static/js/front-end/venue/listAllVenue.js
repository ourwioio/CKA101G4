document.addEventListener('DOMContentLoaded', function () {
	var toggleBtn = document.getElementById('filterToggle');
	var form = document.getElementById('searchForm');
	if (!toggleBtn || !form) return;

	function setOpen(open) {
		form.classList.toggle('is-open', open);
		toggleBtn.setAttribute('aria-expanded', String(open));
	}

	var hasActiveFilter = Array.prototype.some.call(
		form.querySelectorAll('input[type="text"], input[type="number"]'),
		function (input) { return input.value.trim() !== ''; }
	) || Array.prototype.some.call(
		form.querySelectorAll('select'),
		function (select) { return select.value !== ''; }
	);

	setOpen(hasActiveFilter);

	toggleBtn.addEventListener('click', function () {
		setOpen(!form.classList.contains('is-open'));
	});
});

document.addEventListener('DOMContentLoaded', function () {
	var PAGE_SIZE = 8;
	var grid = document.getElementById('venueCardGrid');
	var pagination = document.getElementById('venuePagination');
	if (!grid || !pagination) return;

	var cards = Array.prototype.slice.call(grid.querySelectorAll('.item-card'));
	var totalPages = Math.ceil(cards.length / PAGE_SIZE);

	if (totalPages <= 1) {
		pagination.hidden = true;
		return;
	}

	var pagesEl = document.getElementById('paginationPages');
	var prevBtn = pagination.querySelector('[data-page-prev]');
	var nextBtn = pagination.querySelector('[data-page-next]');
	var currentPage = 1;

	function showPage(page) {
		cards.forEach(function (card, index) {
			var cardPage = Math.floor(index / PAGE_SIZE) + 1;
			card.style.display = cardPage === page ? '' : 'none';
		});
	}

	function renderPageButtons() {
		pagesEl.innerHTML = '';
		for (var i = 1; i <= totalPages; i++) {
			var btn = document.createElement('button');
			btn.type = 'button';
			btn.className = 'pagination__page' + (i === currentPage ? ' is-active' : '');
			btn.textContent = String(i);
			btn.setAttribute('aria-label', '第 ' + i + ' 頁');
			btn.addEventListener('click', (function (page) {
				return function () { goToPage(page, true); };
			})(i));
			pagesEl.appendChild(btn);
		}
	}

	function goToPage(page, scrollToTop) {
		currentPage = Math.min(Math.max(page, 1), totalPages);
		showPage(currentPage);
		renderPageButtons();
		prevBtn.disabled = currentPage === 1;
		nextBtn.disabled = currentPage === totalPages;
		if (scrollToTop) {
			grid.scrollIntoView({ behavior: 'smooth', block: 'start' });
		}
	}

	prevBtn.addEventListener('click', function () { goToPage(currentPage - 1, true); });
	nextBtn.addEventListener('click', function () { goToPage(currentPage + 1, true); });

	goToPage(1, false);
});
