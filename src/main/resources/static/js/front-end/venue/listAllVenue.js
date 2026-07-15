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
