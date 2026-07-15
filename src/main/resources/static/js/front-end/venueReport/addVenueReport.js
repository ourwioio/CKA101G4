document.addEventListener('DOMContentLoaded', function () {
	var reportTextarea = document.getElementById('serReportCom');
	var charCount = document.getElementById('charCount');

	reportTextarea.addEventListener('input', function () {
		charCount.textContent = this.value.length;
	});

	document.getElementById('reportForm').addEventListener('submit', function (e) {
		if (!reportTextarea.value.trim()) {
			e.preventDefault();
			alert('請填寫檢舉內容！');
			return;
		}
		if (!confirm('確定送出檢舉？送出後將由平台人員進行審核。')) {
			e.preventDefault();
		}
	});
});
