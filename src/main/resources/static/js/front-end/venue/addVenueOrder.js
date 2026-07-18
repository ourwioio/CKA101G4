let selectedSlotStatus = '';

document.addEventListener('DOMContentLoaded', function() {
    // 綁定日期按鈕點擊事件
    document.querySelectorAll('.slot-btn.slot-available').forEach(function(btn) {
        btn.addEventListener('click', function() {
            selectSlot(this);
        });
    });

    // 綁定開始時間變更事件
    document.getElementById('startHour').addEventListener('change', function() {
        updateEndHourOptions();
        calculateTotal();
    });

    // 綁定結束時間變更事件
    document.getElementById('endHour').addEventListener('change', function() {
        calculateTotal();
    });

    // 頁面一開始，時段選單先清空（沒選日期前不會有任何小時選項）
    updateStartHourOptions();

    // 🌟 如果是驗證失敗被導回來的畫面，重新帶回之前選的日期跟時段
    restorePreviousSelection();
});

function selectSlot(btn) {
    // 清除所有選取狀態
    document.querySelectorAll('.slot-btn.slot-available').forEach(b => b.classList.remove('selected'));
    btn.classList.add('selected');

    // 帶入選取的值
    document.getElementById('selectedSlotId').value = btn.dataset.slotId;
    document.getElementById('selectedSlotDate').value = btn.dataset.slotDate;

    // 根據這天的 slotStatus 顯示可用時段
    selectedSlotStatus = btn.dataset.slotStatus;
    updateStartHourOptions();
    document.getElementById('totalAmount').style.display = 'none';
}

function updateStartHourOptions() {
    const startSelect = document.getElementById('startHour');
    const endSelect = document.getElementById('endHour');
    startSelect.innerHTML = '<option value="">開始時間</option>';
    endSelect.innerHTML = '<option value="">結束時間</option>';

    if (!selectedSlotStatus) return;

    // 如果選的是今天，只能約現在這個小時「之後」的時段
    const selectedDate = document.getElementById('selectedSlotDate').value;
    const now = new Date();
    const todayStr = now.getFullYear() + '-' + String(now.getMonth() + 1).padStart(2, '0') + '-' + String(now.getDate()).padStart(2, '0');
    const isToday = selectedDate === todayStr;
    const currentHour = now.getHours();

    for (let h = 0; h < 24; h++) {
        if (isToday && h <= currentHour) {
            continue;
        }
        if (selectedSlotStatus[h] === '0') {
            let opt = document.createElement('option');
            opt.value = h;
            opt.textContent = h + ':00';
            startSelect.appendChild(opt);
        }
    }
}

function updateEndHourOptions() {
    const startHour = parseInt(document.getElementById('startHour').value);
    const endSelect = document.getElementById('endHour');
    endSelect.innerHTML = '<option value="">結束時間</option>';

    if (isNaN(startHour)) return;

    for (let h = startHour + 1; h <= 24; h++) {
        // 結束時間 h 可選的條件是：前一個小時 h-1 是可用的（值為 '0'）
        if (selectedSlotStatus[h - 1] === '0') {
            let opt = document.createElement('option');
            opt.value = h;
            opt.textContent = h + ':00';
            endSelect.appendChild(opt);
        } else {
            break;
        }
    }
}

function calculateTotal() {
    const start = parseInt(document.getElementById('startHour').value);
    const end = parseInt(document.getElementById('endHour').value);
    const hourlyRate = parseInt(document.getElementById('hourlyRate').value);

    if (!isNaN(start) && !isNaN(end) && end > start) {
        const hours = end - start;
        const total = hours * hourlyRate;
        document.getElementById('totalAmountText').textContent = total;
        document.getElementById('totalAmount').style.display = 'block';
    } else {
        document.getElementById('totalAmount').style.display = 'none';
    }
}

/** 🌟 驗證失敗被導回來時，重建之前選過的日期跟時段狀態 */
function restorePreviousSelection() {
    if (!restoreVenueSlotId) return;

    const btn = document.querySelector('.slot-btn[data-slot-id="' + restoreVenueSlotId + '"]');
    if (!btn) return;

    selectSlot(btn);

}

function validateForm() {
    return true;
}
