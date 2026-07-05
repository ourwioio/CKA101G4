function updateEndHourOptions() {
    const startHour = parseInt(document.getElementById("startHour").value);
    const endHourSelect = document.getElementById("endHour");
    const currentEnd = endHourSelect.value;

    endHourSelect.innerHTML = '';
    for (let h = startHour + 1; h <= 24; h++) {
        let option = document.createElement("option");
        option.value = h;
        option.textContent = h + ":00";
        if (h == currentEnd) option.selected = true;
        endHourSelect.appendChild(option);
    }
}

// 點擊現有照片，設為封面（標記要送到後端的 coverImageId）
function setExistingCover(imagesId, element) {
    document.getElementById('coverImageId').value = imagesId;
    document.querySelectorAll('.gallery-item').forEach(item => {
        item.classList.remove('is-cover');
        item.querySelector('.cover-tag').textContent = '設為封面';
    });
    element.classList.add('is-cover');
    element.querySelector('.cover-tag').textContent = '目前封面';
}


document.addEventListener("DOMContentLoaded", updateEndHourOptions);
