// 訪客點擊需登入的功能時，攔截並顯示登入提示視窗
document.addEventListener('DOMContentLoaded', function () {
	var modal = document.getElementById('loginModal');
	if (!modal) return; // 已登入時不會渲染 modal，直接放行

	document.querySelectorAll('[data-require-login]').forEach(function (el) {
		el.addEventListener('click', function (e) {
			e.preventDefault();
			modal.hidden = false;
		});
	});

	modal.querySelectorAll('[data-login-modal-close]').forEach(function (el) {
		el.addEventListener('click', function () {
			modal.hidden = true;
		});
	});

	document.addEventListener('keydown', function (e) {
		if (e.key === 'Escape' && !modal.hidden) {
			modal.hidden = true;
		}
	});
});
