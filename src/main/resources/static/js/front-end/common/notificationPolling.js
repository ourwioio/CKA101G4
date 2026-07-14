(() => {
    'use strict';

    const POLLING_INTERVAL = 1000;

    let isRequesting = false;

    async function refreshUnreadBadge() {

        const notifyButton =
            document.querySelector('.icon-btn--notify');

        if (!notifyButton) {
            console.log('目前頁面找不到通知按鈕');
            return;
        }

        if (isRequesting) {
            return;
        }

        isRequesting = true;

        const unreadUrl =
            notifyButton.dataset.unreadUrl
            || '/front/notification/unread-count';

        try {
            const response = await fetch(
                unreadUrl,
                {
                    method: 'GET',
                    credentials: 'same-origin',
                    cache: 'no-store',
                    headers: {
                        'Accept': 'application/json'
                    }
                }
            );

            console.log(
                '通知 API 狀態：',
                response.status
            );

            if (!response.ok) {
                console.error(
                    '通知 API 請求失敗：',
                    response.status
                );
                return;
            }

            const responseText =
                await response.text();

            console.log(
                '後端回傳的未讀數：',
                responseText
            );

            const count =
                Number(responseText);

            if (!Number.isFinite(count)) {
                console.error(
                    '後端回傳的不是有效數字：',
                    responseText
                );
                return;
            }

            let badge =
                notifyButton.querySelector(
                    '.icon-badge--unread'
                );

            if (count > 0) {

                if (!badge) {
                    badge =
                        document.createElement('span');

                    badge.className =
                        'icon-badge icon-badge--unread';

                    notifyButton.appendChild(badge);
                }

                badge.textContent =
                    count > 99
                        ? '99+'
                        : String(count);

            } else if (badge) {
                badge.remove();
            }

        } catch (error) {
            console.error(
                '取得未讀通知數量失敗：',
                error
            );

        } finally {
            isRequesting = false;
        }
    }

    function startNotificationPolling() {

        console.log(
            '通知輪詢已啟動，每 1 秒查詢一次'
        );

        // 進入頁面立即查一次
        refreshUnreadBadge();

        // 之後每秒查一次
        window.setInterval(
            refreshUnreadBadge,
            POLLING_INTERVAL
        );
    }

    if (document.readyState === 'loading') {

        document.addEventListener(
            'DOMContentLoaded',
            startNotificationPolling
        );

    } else {
        startNotificationPolling();
    }
})();