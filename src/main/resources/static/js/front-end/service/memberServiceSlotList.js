document.addEventListener('DOMContentLoaded', function () {

    /* ==========================================================
       DOM
       ========================================================== */

    const addSlotForm =
        document.getElementById('addSlotForm');

    const slotDateInput =
        document.getElementById('slotDate');

    const startTimeInput =
        document.getElementById('startTime');

    const endTimeInput =
        document.getElementById('endTime');

    const endDayOffsetSelect =
        document.getElementById('endDayOffset');

    const splitMinutesSelect =
        document.getElementById('splitMinutes');

    const slotFormError =
        document.getElementById('slotFormError');

    const addSlotSubmit =
        document.getElementById('addSlotSubmit');

    const slotList =
        document.getElementById('memberSlotList');

    const filteredSlotEmpty =
        document.getElementById('filteredSlotEmpty');

    const filterButtons =
        document.querySelectorAll(
            '.member-slot-filter-button[data-filter]'
        );

    const pageData =
        document.getElementById('slotPageData');

    const serviceId =
        pageData
            ? pageData.dataset.serviceId
            : '';

    /* ==========================================================
       日期限制
       ========================================================== */

    function formatDateInput(date) {

        const year =
            date.getFullYear();

        const month =
            String(date.getMonth() + 1)
                .padStart(2, '0');

        const day =
            String(date.getDate())
                .padStart(2, '0');

        return year + '-' + month + '-' + day;
    }

    if (slotDateInput) {

        const today =
            new Date();

        today.setHours(0, 0, 0, 0);

        const maxBookingDays =
            Number(
                addSlotForm.dataset.maxBookingDays || 14
            );

        const maximumDate =
            new Date(today);

        maximumDate.setDate(
            maximumDate.getDate() + maxBookingDays
        );

        slotDateInput.min =
            formatDateInput(today);

        slotDateInput.max =
            formatDateInput(maximumDate);
    }

    /* ==========================================================
       新增表單驗證
       ========================================================== */

    function hideFormError() {

        if (!slotFormError) {
            return;
        }

        slotFormError.textContent = '';
        slotFormError.setAttribute('hidden', '');
    }

    function showFormError(message) {

        if (!slotFormError) {
            return;
        }

        slotFormError.textContent = message;
        slotFormError.removeAttribute('hidden');
    }

    function combineDateAndTime(
        dateText,
        timeText,
        dayOffset
    ) {

        if (!dateText || !timeText) {
            return null;
        }

        const dateTime =
            new Date(dateText + 'T' + timeText + ':00');

        dateTime.setDate(
            dateTime.getDate() + dayOffset
        );

        return dateTime;
    }

    function validateSlotForm() {

        hideFormError();

        const slotDate =
            slotDateInput.value;

        const startTime =
            startTimeInput.value;

        const endTime =
            endTimeInput.value;

        const endDayOffset =
            Number(endDayOffsetSelect.value || 0);

        const splitMinutes =
            Number(splitMinutesSelect.value || 0);

        if (!slotDate) {
            showFormError('請選擇服務日期');
            slotDateInput.focus();
            return false;
        }

        if (!startTime) {
            showFormError('請選擇開始時間');
            startTimeInput.focus();
            return false;
        }

        if (!endTime) {
            showFormError('請選擇結束時間');
            endTimeInput.focus();
            return false;
        }

        const startDateTime =
            combineDateAndTime(
                slotDate,
                startTime,
                0
            );

        const endDateTime =
            combineDateAndTime(
                slotDate,
                endTime,
                endDayOffset
            );

        if (!startDateTime || !endDateTime) {
            showFormError('時段格式不正確');
            return false;
        }

        const now =
            new Date();

        if (startDateTime <= now) {
            showFormError('開始時間必須晚於現在');
            startTimeInput.focus();
            return false;
        }

        if (endDateTime <= startDateTime) {
            showFormError('結束時間必須晚於開始時間');
            endTimeInput.focus();
            return false;
        }

        const durationMinutes =
            Math.round(
                (endDateTime - startDateTime) /
                60000
            );

        if (durationMinutes > 12 * 60) {
            showFormError('單次新增區間不可超過 12 小時');
            return false;
        }

        if (
            splitMinutes > 0 &&
            durationMinutes % splitMinutes !== 0
        ) {

            showFormError(
                '目前區間共 ' +
                durationMinutes +
                ' 分鐘，無法整除每 ' +
                splitMinutes +
                ' 分鐘的切分方式'
            );

            splitMinutesSelect.focus();

            return false;
        }

        return true;
    }

    if (addSlotForm) {

        [
            slotDateInput,
            startTimeInput,
            endTimeInput,
            endDayOffsetSelect,
            splitMinutesSelect
        ].forEach(function (element) {

            if (!element) {
                return;
            }

            element.addEventListener(
                'change',
                hideFormError
            );
        });

        addSlotForm.addEventListener(
            'submit',
            function (event) {

                if (!validateSlotForm()) {

                    event.preventDefault();

                    return;
                }

                if (addSlotSubmit) {

                    addSlotSubmit.disabled = true;

                    addSlotSubmit.textContent =
                        '新增中...';
                }
            }
        );
    }

    /* ==========================================================
       狀態篩選
       ========================================================== */

    function filterSlots(filterValue) {

        if (!slotList) {
            return;
        }

        const cards =
            slotList.querySelectorAll(
                '.member-slot-card'
            );

        let visibleCount = 0;

        cards.forEach(function (card) {

            const status =
                card.dataset.status;

            const shouldShow =
                filterValue === 'all' ||
                status === filterValue;

            card.hidden =
                !shouldShow;

            if (shouldShow) {
                visibleCount++;
            }
        });

        if (filteredSlotEmpty) {

            if (visibleCount === 0) {

                filteredSlotEmpty.removeAttribute(
                    'hidden'
                );

            } else {

                filteredSlotEmpty.setAttribute(
                    'hidden',
                    ''
                );
            }
        }
    }

    filterButtons.forEach(function (button) {

        button.addEventListener(
            'click',
            function () {

                filterButtons.forEach(
                    function (otherButton) {

                        otherButton.classList.remove(
                            'active'
                        );
                    }
                );

                button.classList.add('active');

                filterSlots(
                    button.dataset.filter
                );
            }
        );
    });

    /* ==========================================================
       單筆刪除時段彈窗
       ========================================================== */

    const deleteSlotModal =
        document.getElementById('deleteSlotModal');

    const deleteSlotForm =
        document.getElementById('deleteSlotForm');

    const deleteSlotTime =
        document.getElementById('deleteSlotTime');

    const cancelDeleteSlot =
        document.getElementById('cancelDeleteSlot');

    const deleteSlotButtons =
        document.querySelectorAll('.js-delete-slot');

    function openDeleteSlotModal(
        slotId,
        slotTime
    ) {

        if (
            !deleteSlotModal ||
            !deleteSlotForm ||
            !serviceId
        ) {
            return;
        }

        deleteSlotForm.action =
            '/member/services/' +
            serviceId +
            '/slots/' +
            slotId +
            '/delete';

        if (deleteSlotTime) {

            deleteSlotTime.textContent =
                slotTime || '此服務時段';
        }

        deleteSlotModal.removeAttribute(
            'hidden'
        );
    }

    function closeDeleteSlotModal() {

        if (!deleteSlotModal) {
            return;
        }

        deleteSlotModal.setAttribute(
            'hidden',
            ''
        );

        if (deleteSlotForm) {
            deleteSlotForm.removeAttribute('action');
        }

        if (deleteSlotTime) {
            deleteSlotTime.textContent = '服務時段';
        }
    }

    deleteSlotButtons.forEach(function (button) {

        button.addEventListener(
            'click',
            function () {

                openDeleteSlotModal(
                    button.dataset.slotId,
                    button.dataset.slotTime
                );
            }
        );
    });

    if (cancelDeleteSlot) {

        cancelDeleteSlot.addEventListener(
            'click',
            closeDeleteSlotModal
        );
    }

    if (deleteSlotModal) {

        deleteSlotModal.addEventListener(
            'click',
            function (event) {

                if (event.target === deleteSlotModal) {
                    closeDeleteSlotModal();
                }
            }
        );
    }

    /* ==========================================================
       清空可預約時段彈窗
       ========================================================== */

    const clearAvailableModal =
        document.getElementById(
            'clearAvailableModal'
        );

    const openClearAvailableModal =
        document.getElementById(
            'openClearAvailableModal'
        );

    const cancelClearAvailable =
        document.getElementById(
            'cancelClearAvailable'
        );

    function openClearModal() {

        if (!clearAvailableModal) {
            return;
        }

        clearAvailableModal.removeAttribute(
            'hidden'
        );
    }

    function closeClearModal() {

        if (!clearAvailableModal) {
            return;
        }

        clearAvailableModal.setAttribute(
            'hidden',
            ''
        );
    }

    if (openClearAvailableModal) {

        openClearAvailableModal.addEventListener(
            'click',
            openClearModal
        );
    }

    if (cancelClearAvailable) {

        cancelClearAvailable.addEventListener(
            'click',
            closeClearModal
        );
    }

    if (clearAvailableModal) {

        clearAvailableModal.addEventListener(
            'click',
            function (event) {

                if (
                    event.target ===
                    clearAvailableModal
                ) {
                    closeClearModal();
                }
            }
        );
    }

    /* ==========================================================
       Escape 關閉彈窗
       ========================================================== */

    document.addEventListener(
        'keydown',
        function (event) {

            if (event.key !== 'Escape') {
                return;
            }

            closeDeleteSlotModal();
            closeClearModal();
        }
    );

    /* ==========================================================
       防止刪除表單重複提交
       ========================================================== */

    document
        .querySelectorAll(
            '.member-slot-modal form'
        )
        .forEach(function (form) {

            form.addEventListener(
                'submit',
                function () {

                    const submitButton =
                        form.querySelector(
                            'button[type="submit"]'
                        );

                    if (submitButton) {

                        submitButton.disabled = true;

                        submitButton.textContent =
                            '處理中...';
                    }
                }
            );
        });
		
		document.addEventListener("DOMContentLoaded", function () {

		    const startTimeInput =
		        document.getElementById("startTime");

		    const endTimeInput =
		        document.getElementById("endTime");

		    const endDayOffsetSelect =
		        document.getElementById("endDayOffset");

		    const addSlotForm =
		        document.getElementById("addSlotForm");

		    const errorBox =
		        document.getElementById("slotFormError");

		    if (!startTimeInput
		            || !endTimeInput
		            || !endDayOffsetSelect
		            || !addSlotForm) {

		        return;
		    }

		    const MINUTES_PER_DAY = 24 * 60;
		    const TIME_STEP_MINUTES = 30;
		    const MAX_DURATION_MINUTES = 12 * 60;


		    // =========================================================
		    // HH:mm 轉成當天分鐘數
		    // 例如：
		    // 12:32 -> 752
		    // =========================================================

		    function timeToMinutes(timeValue) {

		        if (!timeValue) {
		            return null;
		        }

		        const parts =
		            timeValue.split(":");

		        const hour =
		            Number(parts[0]);

		        const minute =
		            Number(parts[1]);

		        return hour * 60 + minute;
		    }


		    // =========================================================
		    // 分鐘數轉回 HH:mm
		    // =========================================================

		    function minutesToTime(totalMinutes) {

		        let normalizedMinutes =
		            totalMinutes % MINUTES_PER_DAY;

		        if (normalizedMinutes < 0) {
		            normalizedMinutes += MINUTES_PER_DAY;
		        }

		        const hour =
		            Math.floor(normalizedMinutes / 60);

		        const minute =
		            normalizedMinutes % 60;

		        return String(hour).padStart(2, "0")
		                + ":"
		                + String(minute).padStart(2, "0");
		    }


		    // =========================================================
		    // 將時間往上對齊到指定的 30 分鐘序列
		    //
		    // 例如基準是 12:32：
		    // 有效時間就是 13:02、13:32、14:02……
		    // =========================================================

		    function alignUp(value, base, step) {

		        const remainder =
		            ((value - base) % step + step) % step;

		        if (remainder === 0) {
		            return value;
		        }

		        return value + step - remainder;
		    }


		    // =========================================================
		    // 重設成任意分鐘
		    // =========================================================

		    function resetTimeConstraints(input) {

		        input.step = "60";

		        input.removeAttribute("min");
		        input.removeAttribute("max");

		        input.setCustomValidity("");
		    }


		    // =========================================================
		    // 檢查目前已選值是否仍符合新的 min/max/step
		    // 不符合就清空，要求重新選擇
		    // =========================================================

		    function clearInvalidValue(input) {

		        if (!input.value) {
		            return;
		        }

		        if (input.validity.rangeUnderflow
		                || input.validity.rangeOverflow
		                || input.validity.stepMismatch) {

		            input.value = "";
		        }
		    }


		    // =========================================================
		    // 已選開始時間：
		    // 限制結束時間只能是開始時間 + 30 分鐘的倍數
		    // =========================================================

		    function updateEndTimeConstraints() {

		        if (!startTimeInput.value) {

		            resetTimeConstraints(
		                endTimeInput
		            );

		            return;
		        }

		        const startMinutes =
		            timeToMinutes(
		                startTimeInput.value
		            );

		        const endDayOffset =
		            Number(
		                endDayOffsetSelect.value
		            ) || 0;

		        const targetDayStart =
		            endDayOffset * MINUTES_PER_DAY;

		        const targetDayEnd =
		            targetDayStart
		            + MINUTES_PER_DAY
		            - 1;

		        // 最早結束：開始時間後 30 分鐘
		        const firstPossibleEnd =
		            startMinutes
		            + TIME_STEP_MINUTES;

		        // 最晚結束：開始時間後 12 小時
		        const latestPossibleEnd =
		            startMinutes
		            + MAX_DURATION_MINUTES;

		        // 對齊目前選擇的結束日期
		        const firstEndOnTargetDay =
		            alignUp(
		                targetDayStart,
		                firstPossibleEnd,
		                TIME_STEP_MINUTES
		            );

		        const minimumEnd =
		            Math.max(
		                firstPossibleEnd,
		                firstEndOnTargetDay
		            );

		        const maximumEnd =
		            Math.min(
		                latestPossibleEnd,
		                targetDayEnd
		            );

		        // 目前日期設定下沒有合法結束時間
		        if (minimumEnd > maximumEnd) {

		            endTimeInput.value = "";

		            endTimeInput.setCustomValidity(
		                "目前的開始時間與結束日期，無法建立 12 小時內的時段"
		            );

		            return;
		        }

		        endTimeInput.setCustomValidity("");

		        /*
		         * step 的單位是秒。
		         * 1800 秒 = 30 分鐘。
		         *
		         * step 會以 min 作為計算基準，
		         * 所以開始 12:32 時：
		         *
		         * min = 13:02
		         * 後續有效時間：
		         * 13:02、13:32、14:02……
		         */
		        endTimeInput.step = "1800";

		        endTimeInput.min =
		            minutesToTime(minimumEnd);

		        endTimeInput.max =
		            minutesToTime(maximumEnd);

		        clearInvalidValue(
		            endTimeInput
		        );
		    }


		    // =========================================================
		    // 已選結束時間：
		    // 限制開始時間只能是結束時間 - 30 分鐘的倍數
		    // =========================================================

		    function updateStartTimeConstraints() {

		        if (!endTimeInput.value) {

		            resetTimeConstraints(
		                startTimeInput
		            );

		            return;
		        }

		        const endDayOffset =
		            Number(
		                endDayOffsetSelect.value
		            ) || 0;

		        const endMinutes =
		            timeToMinutes(
		                endTimeInput.value
		            )
		            + endDayOffset * MINUTES_PER_DAY;

		        // 最早開始：結束前最多 12 小時
		        const earliestStart =
		            Math.max(
		                0,
		                endMinutes
		                - MAX_DURATION_MINUTES
		            );

		        // 最晚開始：結束前至少 30 分鐘
		        const latestStart =
		            Math.min(
		                MINUTES_PER_DAY - 1,
		                endMinutes
		                - TIME_STEP_MINUTES
		            );

		        /*
		         * 開始時間必須和結束時間具有相同的
		         * 30 分鐘餘數。
		         *
		         * 例如結束 14:32：
		         * 開始可以是：
		         * 14:02、13:32、13:02……
		         */
		        const firstValidStart =
		            alignUp(
		                earliestStart,
		                endMinutes,
		                TIME_STEP_MINUTES
		            );

		        if (firstValidStart > latestStart) {

		            startTimeInput.value = "";

		            startTimeInput.setCustomValidity(
		                "目前的結束時間與結束日期，無法建立有效時段"
		            );

		            return;
		        }

		        startTimeInput.setCustomValidity("");

		        startTimeInput.step = "1800";

		        startTimeInput.min =
		            minutesToTime(
		                firstValidStart
		            );

		        startTimeInput.max =
		            minutesToTime(
		                latestStart
		            );

		        clearInvalidValue(
		            startTimeInput
		        );
		    }


		    // =========================================================
		    // 顯示錯誤訊息
		    // =========================================================

		    function showTimeError(message) {

		        if (!errorBox) {
		            return;
		        }

		        errorBox.textContent =
		            message;

		        errorBox.hidden =
		            false;
		    }


		    function clearTimeError() {

		        if (!errorBox) {
		            return;
		        }

		        errorBox.textContent = "";

		        errorBox.hidden = true;
		    }


		    // =========================================================
		    // 開始時間改變
		    // =========================================================

		    startTimeInput.addEventListener(
		        "change",
		        function () {

		            clearTimeError();

		            updateEndTimeConstraints();
		        }
		    );


		    // =========================================================
		    // 結束時間改變
		    // =========================================================

		    endTimeInput.addEventListener(
		        "change",
		        function () {

		            clearTimeError();

		            updateStartTimeConstraints();
		        }
		    );


		    // =========================================================
		    // 同一天／隔天改變
		    // =========================================================

		    endDayOffsetSelect.addEventListener(
		        "change",
		        function () {

		            clearTimeError();

		            if (startTimeInput.value) {
		                updateEndTimeConstraints();
		            }

		            if (endTimeInput.value) {
		                updateStartTimeConstraints();
		            }
		        }
		    );


		    // =========================================================
		    // 送出前再次檢查
		    // =========================================================

		    addSlotForm.addEventListener(
		        "submit",
		        function (event) {

		            clearTimeError();

		            if (!startTimeInput.value
		                    || !endTimeInput.value) {

		                return;
		            }

		            const startMinutes =
		                timeToMinutes(
		                    startTimeInput.value
		                );

		            const endMinutes =
		                timeToMinutes(
		                    endTimeInput.value
		                )
		                + Number(
		                    endDayOffsetSelect.value
		                ) * MINUTES_PER_DAY;

		            const durationMinutes =
		                endMinutes
		                - startMinutes;

		            if (durationMinutes <= 0) {

		                event.preventDefault();

		                showTimeError(
		                    "結束時間必須晚於開始時間"
		                );

		                return;
		            }

		            if (durationMinutes
		                    % TIME_STEP_MINUTES !== 0) {

		                event.preventDefault();

		                showTimeError(
		                    "開始時間與結束時間必須相差 30 分鐘的倍數"
		                );

		                return;
		            }

		            if (durationMinutes
		                    > MAX_DURATION_MINUTES) {

		                event.preventDefault();

		                showTimeError(
		                    "單次建立的時段最長為 12 小時"
		                );
		            }
		        }
		    );
		});
});