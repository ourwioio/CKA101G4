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
});