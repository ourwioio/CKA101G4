document.addEventListener("DOMContentLoaded", function() {
    /* ---- 開始/結束時間連動 ---- */
    const startHourSelect = document.getElementById("startHour");
    const endHourSelect = document.getElementById("endHour");

    if (startHourSelect && endHourSelect) {
        startHourSelect.addEventListener("change", updateEndHourOptions);
        // 頁面載入時（例如表單驗證失敗回填 savedStartHour）也要先套用一次篩選
        updateEndHourOptions();
    }
});

function updateEndHourOptions() {
    const startHourSelect = document.getElementById("startHour");
    const endHourSelect = document.getElementById("endHour");

    if (!startHourSelect.value) {
        return; // 尚未選擇開始時間，結束時間選項維持原樣
    }

    const startHour = parseInt(startHourSelect.value, 10);
    const options = Array.from(endHourSelect.querySelectorAll("option"));

    let firstValidOption = null;
    let currentIsValid = false;

    options.forEach(function(option) {
        if (option.value === "") {
            return; // 保留「請選擇」之類的預設選項（如果有的話）
        }
        const value = parseInt(option.value, 10);
        const isValid = value > startHour;

        option.hidden = !isValid;
        option.disabled = !isValid;

        if (isValid) {
            if (!firstValidOption) {
                firstValidOption = option;
            }
            if (option.selected) {
                currentIsValid = true;
            }
        }
    });

    // 若目前選到的結束時間已經不合法（<= 開始時間），自動跳到開始時間之後最近的一個時段
    if (!currentIsValid && firstValidOption) {
        endHourSelect.value = firstValidOption.value;
    }
}

// 目前選擇要上傳的照片（用陣列自己管理，才能支援刪除單張）
let selectedFiles = [];
let coverIndex = 0;

function previewImages() {
    const input = document.getElementById('upFiles');
    const newFiles = Array.from(input.files);

    // 累加而不是取代：原生 file input 每次選檔都會整個換成「這次選的檔案」，
    // 所以要自己把新選的檔案併進既有的 selectedFiles，才能一張一張慢慢加
    const wasEmpty = selectedFiles.length === 0;
    selectedFiles = selectedFiles.concat(newFiles);

    if (wasEmpty) {
        coverIndex = 0; // 第一批照片，預設第一張當封面
    }

    renderPreviews();
}

function renderPreviews() {
    const previewArea = document.getElementById('previewArea');
    previewArea.innerHTML = '';

    selectedFiles.forEach((file, index) => {
        const reader = new FileReader();
        reader.onload = function(e) {
            const isCover = index === coverIndex;

            const item = document.createElement('div');
            item.className = 'preview-item' + (isCover ? ' is-cover' : '');
            item.onclick = () => setCover(index);

            const img = document.createElement('img');
            img.src = e.target.result;
            item.appendChild(img);

            const removeBtn = document.createElement('div');
            removeBtn.className = 'remove-btn';
            removeBtn.textContent = '✕';
            removeBtn.title = '不上傳這張';
            removeBtn.onclick = function(e) {
                e.stopPropagation(); // 避免同時觸發設封面
                removeFile(index);
            };
            item.appendChild(removeBtn);

            const tag = document.createElement('div');
            tag.className = 'cover-tag';
            tag.textContent = isCover ? '目前封面' : '設為封面';
            item.appendChild(tag);

            previewArea.appendChild(item);
        };
        reader.readAsDataURL(file);
    });

    document.getElementById('coverIndex').value = coverIndex;
    syncFileInput();
}

function removeFile(index) {
    selectedFiles.splice(index, 1);

    if (selectedFiles.length === 0) {
        coverIndex = 0;
    } else if (index === coverIndex) {
        coverIndex = 0; // 封面被刪掉了，改用剩下的第一張當封面
    } else if (index < coverIndex) {
        coverIndex -= 1; // 前面被移除一張，原本封面的位置要跟著往前遞補
    }

    renderPreviews();
}

function setCover(index) {
    coverIndex = index;
    renderPreviews();
}

// <input type="file"> 的 files 是唯讀的，不能直接刪除其中一個檔案，
// 所以刪除照片後要用 DataTransfer 重新組一份 FileList 塞回真正的 input，
// 表單送出時才會真的排除掉被刪掉的照片。
function syncFileInput() {
    const input = document.getElementById('upFiles');
    const dataTransfer = new DataTransfer();
    selectedFiles.forEach(file => dataTransfer.items.add(file));
    input.files = dataTransfer.files;
}