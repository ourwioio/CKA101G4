(() => {
    'use strict';

    const RECONNECT_DELAY_MS = 3000;
    const NOTIFICATION_REFRESH_MS = 30000;

    let stompClient = null;
    let reconnectTimer = null;
    let pageUnloading = false;

    document.addEventListener('DOMContentLoaded', () => {
        initializeBookingForms();
        initializeUnreadBadge();
        initializeSlotWebSocket();
    });

    window.addEventListener('beforeunload', () => {
        pageUnloading = true;

        if (reconnectTimer !== null) {
            window.clearTimeout(reconnectTimer);
        }

        if (stompClient?.connected) {
            stompClient.disconnect(() => {});
        }
    });

    // =========================================================
    // 預約表單展開／收合
    // =========================================================

    function initializeBookingForms() {
        document.addEventListener('click', (event) => {
            const toggleButton = event.target.closest(
                '.service-booking-toggle[data-target]'
            );

            if (toggleButton && toggleButton.tagName === 'BUTTON') {
                toggleBookingForm(toggleButton);
                return;
            }

            const cancelButton = event.target.closest(
                '.service-booking-cancel[data-target]'
            );

            if (cancelButton) {
                closeBookingForm(cancelButton.dataset.target);
            }
        });
    }

    function toggleBookingForm(button) {
        const targetId = button.dataset.target;
        const targetForm = document.getElementById(targetId);

        if (!targetForm) {
            return;
        }

        const willOpen = targetForm.hidden;
        closeAllBookingForms(targetForm);

        targetForm.hidden = !willOpen;
        button.textContent = willOpen ? '收起預約需求' : '我要預約';

        if (willOpen) {
            targetForm.querySelector('textarea')?.focus();
        }
    }

    function closeBookingForm(targetId) {
        const targetForm = document.getElementById(targetId);

        if (!targetForm) {
            return;
        }

        targetForm.hidden = true;

        const toggleButton = document.querySelector(
            `.service-booking-toggle[data-target="${escapeSelector(targetId)}"]`
        );

        if (toggleButton && toggleButton.tagName === 'BUTTON') {
            toggleButton.textContent = '我要預約';
        }
    }

    function closeAllBookingForms(exceptForm = null) {
        document.querySelectorAll('.service-booking-form').forEach((form) => {
            if (form === exceptForm) {
                return;
            }

            form.hidden = true;

            const toggleButton = document.querySelector(
                `.service-booking-toggle[data-target="${escapeSelector(form.id)}"]`
            );

            if (toggleButton && toggleButton.tagName === 'BUTTON') {
                toggleButton.textContent = '我要預約';
            }
        });
    }

    function escapeSelector(value) {
        if (window.CSS?.escape) {
            return window.CSS.escape(value);
        }

        return String(value).replace(/(["\\])/g, '\\$1');
    }

    // =========================================================
    // WebSocket：服務時段即時狀態
    // =========================================================

    function initializeSlotWebSocket() {
        const pageData = document.getElementById('serviceDetailPageData');

        if (!pageData) {
            return;
        }

        const serviceId = Number(pageData.dataset.serviceId);
        const websocketUrl = pageData.dataset.websocketUrl;
        const topic = pageData.dataset.topic;

        if (!Number.isInteger(serviceId) || !websocketUrl || !topic) {
            console.warn('缺少服務時段 WebSocket 設定');
            return;
        }

        if (typeof window.SockJS !== 'function' || typeof window.Stomp === 'undefined') {
            console.error('SockJS 或 STOMP 尚未載入');
            return;
        }

        connectSlotWebSocket({ serviceId, websocketUrl, topic });
    }

    function connectSlotWebSocket(config) {
        if (pageUnloading) {
            return;
        }

        try {
            const socket = new window.SockJS(config.websocketUrl);
            const client = window.Stomp.over(socket);

            // 關閉 STOMP 預設大量除錯訊息。
            client.debug = () => {};

            client.connect(
                {},
                () => {
                    stompClient = client;
                    console.info('服務時段 WebSocket 已連線');

                    client.subscribe(config.topic, (frame) => {
                        handleSlotStatusMessage(frame, config.serviceId);
                    });
                },
                (error) => {
                    console.warn('服務時段 WebSocket 連線中斷', error);
                    scheduleReconnect(config);
                }
            );
        } catch (error) {
            console.error('建立服務時段 WebSocket 失敗', error);
            scheduleReconnect(config);
        }
    }

    function scheduleReconnect(config) {
        if (pageUnloading || reconnectTimer !== null) {
            return;
        }

        reconnectTimer = window.setTimeout(() => {
            reconnectTimer = null;
            connectSlotWebSocket(config);
        }, RECONNECT_DELAY_MS);
    }

    function handleSlotStatusMessage(frame, currentServiceId) {
        let message;

        try {
            message = JSON.parse(frame.body);
        } catch (error) {
            console.error('無法解析服務時段 WebSocket 訊息', error);
            return;
        }

        const serviceId = Number(message.serviceId);
        const slotId = Number(message.serviceSlotId);
        const slotStatus = Number(message.slotStatus);

        if (serviceId !== currentServiceId) {
            return;
        }

        if (!Number.isInteger(slotId) || !Number.isInteger(slotStatus)) {
            console.warn('服務時段 WebSocket 訊息格式不完整', message);
            return;
        }

        updateSlotCard(slotId, slotStatus, message.statusText);
    }

    function updateSlotCard(slotId, slotStatus, statusText) {
        const card = document.querySelector(
            `.service-slot-item[data-slot-id="${slotId}"]`
        );

        if (!card) {
            return;
        }

        card.dataset.slotStatus = String(slotStatus);

        card.classList.remove(
            'service-slot-item--available',
            'service-slot-item--locked',
            'service-slot-item--booked'
        );

        const statusBadge = card.querySelector('.js-slot-status');
        const availableContent = card.querySelector('.js-slot-available-content');
        const lockedContent = card.querySelector('.js-slot-locked-content');
        const bookedContent = card.querySelector('.js-slot-booked-content');
        const bookedMessage = card.querySelector('.js-slot-booked-message');

        closeCardBookingForm(card);

        if (statusBadge) {
            statusBadge.classList.remove('available', 'locked', 'booked');
        }

        if (slotStatus === 0) {
            card.classList.add('service-slot-item--available');
            setHidden(availableContent, false);
            setHidden(lockedContent, true);
            setHidden(bookedContent, true);
            setStatusBadge(statusBadge, 'available', statusText || '可預約');
            return;
        }

        if (slotStatus === 1) {
            card.classList.add('service-slot-item--locked');
            setHidden(availableContent, true);
            setHidden(lockedContent, false);
            setHidden(bookedContent, true);
            setStatusBadge(statusBadge, 'locked', statusText || '暫時鎖定');

            const lockTime = card.querySelector('.js-slot-lock-time');
            if (lockTime) {
                lockTime.textContent = '此時段已鎖定 30 秒，等待獲選買家付款';
            }
            return;
        }

        card.classList.add('service-slot-item--booked');
        setHidden(availableContent, true);
        setHidden(lockedContent, true);
        setHidden(bookedContent, false);
        setStatusBadge(
            statusBadge,
            'booked',
            statusText || (slotStatus === 2 ? '已預約' : '無法預約')
        );

        if (bookedMessage) {
            bookedMessage.textContent = slotStatus === 2
                ? '此時段已經完成預約，無法再次選擇。'
                : '此時段目前無法預約。';
        }
    }

    function setStatusBadge(element, className, text) {
        if (!element) {
            return;
        }

        element.classList.add(className);
        element.textContent = text;
    }

    function setHidden(element, hidden) {
        if (element) {
            element.hidden = hidden;
        }
    }

    function closeCardBookingForm(card) {
        const form = card.querySelector('.service-booking-form');
        const button = card.querySelector(
            '.service-booking-toggle[data-target]'
        );

        if (form) {
            form.hidden = true;
        }

        if (button && button.tagName === 'BUTTON') {
            button.textContent = '我要預約';
        }
    }

    // =========================================================
    // 通知未讀數量
    // =========================================================

    function initializeUnreadBadge() {
        refreshUnreadBadge();
        window.setInterval(refreshUnreadBadge, NOTIFICATION_REFRESH_MS);
    }

    async function refreshUnreadBadge() {
        try {
            const response = await fetch('/notification/unread-count', {
                credentials: 'same-origin',
                cache: 'no-store'
            });

            if (!response.ok) {
                return;
            }

            const count = Number(await response.json());
            const notifyButton = document.querySelector('.icon-btn--notify');

            if (!notifyButton || !Number.isFinite(count)) {
                return;
            }

            let badge = notifyButton.querySelector('.icon-badge--unread');

            if (count > 0) {
                if (!badge) {
                    badge = document.createElement('span');
                    badge.className = 'icon-badge icon-badge--unread';
                    notifyButton.appendChild(badge);
                }

                badge.textContent = count > 99 ? '99+' : String(count);
            } else {
                badge?.remove();
            }
        } catch (error) {
            console.error('更新通知數量失敗：', error);
        }
    }
})();
