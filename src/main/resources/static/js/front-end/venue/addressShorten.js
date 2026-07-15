// 將完整地址縮短為「縣市＋鄉鎮市區」，找不到對應字元時保留原字串
function shortenAddress(address) {
	if (!address) return address;
	var cityMatch = address.match(/^.*?[市縣]/);
	if (!cityMatch) return address;
	var rest = address.slice(cityMatch[0].length);
	var districtMatch = rest.match(/^.*?[區鄉鎮市]/);
	if (!districtMatch) return cityMatch[0];
	return cityMatch[0] + districtMatch[0];
}

document.addEventListener('DOMContentLoaded', function () {
	document.querySelectorAll('.item-card__addr').forEach(function (el) {
		var full = el.textContent.trim();
		var short = shortenAddress(full);
		if (short !== full) {
			el.textContent = short;
			el.title = full;
		}
	});
});
