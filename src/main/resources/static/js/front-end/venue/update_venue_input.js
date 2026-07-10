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

// 點擊「既有」照片，設為封面
function setExistingCover(imagesId, element) {
    document.getElementById('coverImageId').value = imagesId;
    document.getElementById('coverNewIndex').value = '';
    clearAllCoverMarks();
    element.classList.add('is-cover');
    element.querySelector('.cover-tag').textContent = '目前封面';
}

// 點擊「這次新上傳」的照片，設為封面
function setNewCover(index, element) {
    document.getElementById('coverNewIndex').value = index;
    document.getElementById('coverImageId').value = '';
    clearAllCoverMarks();
    element.classList.add('is-cover');
    element.querySelector('.cover-tag').textContent = '目前封面';
}

function clearAllCoverMarks() {
    document.querySelectorAll('.gallery-item').forEach(item => {
        item.classList.remove('is-cover');
        const tag = item.querySelector('.cover-tag');
        if (tag) tag.textContent = '設為封面';
    });
}

// 選擇新照片後產生預覽縮圖
let selectedFiles = [];

function handleFileSelect(event) {
    selectedFiles = Array.from(event.target.files);
    renderNewPreviews();
}

function renderNewPreviews() {
    const container = document.getElementById('newPreviewGallery');
    container.innerHTML = '';

    selectedFiles.forEach((file, index) => {
        const reader = new FileReader();
        reader.onload = function (e) {
            const item = document.createElement('div');
            item.className = 'gallery-item';
            item.onclick = function () { setNewCover(index, item); };

            const img = document.createElement('img');
            img.src = e.target.result;

            const tag = document.createElement('div');
            tag.className = 'cover-tag';
            tag.textContent = '設為封面';

            item.appendChild(img);
            item.appendChild(tag);
            container.appendChild(item);
        };
        reader.readAsDataURL(file);
    });
}

function toggleDeleteMark(imagesId, linkElement) {
    const item = document.getElementById('img-item-' + imagesId);
    const checkbox = document.getElementById('del-check-' + imagesId);
    if (!item || !checkbox) return;

    checkbox.checked = !checkbox.checked;
    item.classList.toggle('is-deleted', checkbox.checked);
    linkElement.textContent = checkbox.checked ? '復原' : '刪除';

    if (checkbox.checked) {
        // 如果被刪除的剛好是目前選的封面，清空封面欄位
        const coverInput = document.getElementById('coverImageId');
        if (coverInput.value == imagesId) {
            coverInput.value = '';
            item.classList.remove('is-cover');
            const tag = item.querySelector('.cover-tag');
            if (tag) tag.textContent = '設為封面';
        }
    }
}

function toggleVenueStatusField() {
    const input = document.getElementById('venueStatusInput');
    const btn = document.getElementById('statusToggleBtn');
    const newStatus = input.value == '0' ? '1' : '0';
    input.value = newStatus;
    btn.textContent = newStatus == '0' ? '目前：下架（點擊改為上架）' : '目前：上架（點擊改為下架）';
}
document.addEventListener("DOMContentLoaded", updateEndHourOptions);