// 點擊 .lightbox-trigger 圖片時，於全螢幕燈箱中放大顯示，並可左右切換
document.addEventListener('DOMContentLoaded', function () {
	var triggers = Array.prototype.slice.call(document.querySelectorAll('.lightbox-trigger'));
	var lightbox = document.getElementById('photoLightbox');
	if (!triggers.length || !lightbox) return;

	var img = document.getElementById('lightboxImg');
	var counter = document.getElementById('lightboxCounter');
	var current = 0;

	lightbox.classList.toggle('lightbox--single', triggers.length <= 1);

	function show(index) {
		current = (index + triggers.length) % triggers.length;
		img.src = triggers[current].src;
		if (counter) counter.textContent = (current + 1) + ' / ' + triggers.length;
	}

	function open(index) {
		show(index);
		lightbox.hidden = false;
	}

	function close() {
		lightbox.hidden = true;
	}

	triggers.forEach(function (el, index) {
		el.addEventListener('click', function () {
			open(index);
		});
	});

	lightbox.querySelectorAll('[data-lightbox-close]').forEach(function (el) {
		el.addEventListener('click', close);
	});

	var prevBtn = lightbox.querySelector('[data-lightbox-prev]');
	var nextBtn = lightbox.querySelector('[data-lightbox-next]');
	if (prevBtn) prevBtn.addEventListener('click', function () { show(current - 1); });
	if (nextBtn) nextBtn.addEventListener('click', function () { show(current + 1); });

	document.addEventListener('keydown', function (e) {
		if (lightbox.hidden) return;
		if (e.key === 'Escape') close();
		if (e.key === 'ArrowLeft') show(current - 1);
		if (e.key === 'ArrowRight') show(current + 1);
	});
});
