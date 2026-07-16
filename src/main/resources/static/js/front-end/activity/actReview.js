let currentStarFilter = 'all';

function filterReviews(targetStars) {
	currentStarFilter = targetStars;

	const buttons = document.querySelectorAll('.filter-btn');
	buttons.forEach(btn => btn.classList.remove('active'));

	if (window.event && window.event.currentTarget) {
		window.event.currentTarget.classList.add('active');
	}

	applyFilterAndSort();
}


function toggleTimeSort() {
	const btn = document.getElementById('sortTimeBtn');
	if (!btn) return;

	const currentOrder = btn.getAttribute('data-sort');

	if (currentOrder === 'desc') {
		btn.setAttribute('data-sort', 'asc');
		btn.innerText = '✓ 最舊優先'; 
		btn.classList.add('active-sort');
	} else {
		btn.setAttribute('data-sort', 'desc');
		btn.innerText = '⏳ 最新優先'; 
		btn.classList.remove('active-sort');
	}

	applyFilterAndSort();
}


function applyFilterAndSort() {
	const bodyContainer = document.getElementById('reviewScrollBody');
	if (!bodyContainer) return;

	const cards = Array.from(bodyContainer.querySelectorAll('.review-card'));
	const sortOrder = document.getElementById('sortTimeBtn')?.getAttribute('data-sort') || 'desc';

	cards.sort((a, b) => {
		const timeA = parseInt(a.getAttribute('data-time') || '0', 10);
		const timeB = parseInt(b.getAttribute('data-time') || '0', 10);

		return sortOrder === 'desc' ? (timeB - timeA) : (timeA - timeB);
	});

	let visibleCount = 0;
	cards.forEach(card => {
		bodyContainer.appendChild(card);

		const cardStars = card.getAttribute('data-stars');
		if (currentStarFilter === 'all' || cardStars === currentStarFilter) {
			card.style.display = 'flex';
			visibleCount++;
		} else {
			card.style.display = 'none';
		}
	});

	const emptyMsg = document.getElementById('filterEmptyMsg');
	if (emptyMsg) {
		emptyMsg.style.display = (visibleCount === 0) ? 'block' : 'none';
	}
}
